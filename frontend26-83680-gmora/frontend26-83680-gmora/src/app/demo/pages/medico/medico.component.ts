import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from 'bootstrap';
import Swal from 'sweetalert2';
import { extraerMensajeError } from 'src/app/helpers/error.helper';
import { normalizarTexto } from 'src/app/helpers/text.helper';
import { Especializacion } from 'src/app/models/especializacion';
import { Medico, MedicoRq } from 'src/app/models/medico';
import { EspecializacionService } from '../especializacion/service/especializacion.service';
import { MedicoService } from './service/medico.service';

@Component({
    selector: 'app-medico',
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './medico.component.html',
    styleUrl: './medico.component.scss'
})
export class MedicoComponent {

    // Variables para el modal.
    modalInstance: Modal | null = null;
    titleModal: string = '';
    modoFormulario: string = '';
    titleBoton: string = '';

    // Variables para la paginación y búsqueda en la datatable.
    listMedicos: Medico[] = [];
    listEspecializaciones: Especializacion[] = [];
    terminoBusqueda: string = '';
    paginaActual: number = 1;
    readonly registrosPorPagina: number = 10;
    cargando: boolean = false;
    errorCarga: boolean = false;
    guardando: boolean = false;

    form!: FormGroup;
    medicoSeleccionado: Medico | null = null;

    constructor(
        private readonly medicoService: MedicoService,
        private readonly especializacionService: EspecializacionService,
        private readonly formBuilder: FormBuilder
    ) {
        this.inicializarFormulario();
        this.listar();
        this.cargarEspecializaciones();
    }

    inicializarFormulario(): void {
        this.form = this.formBuilder.group({
            tipoDocumento: ['CC', Validators.required],
            numeroDocumento: ['', Validators.required],
            nombres: ['', Validators.required],
            apellidos: ['', Validators.required],
            telefono: [''],
            registroProfesional: ['', Validators.required],
            especializacionId: [null as number | null, Validators.required]
        });
    }

    get f(): { [key: string]: AbstractControl } {
        return this.form.controls;
    }

    get medicoFiltrados(): Medico[] {
        const termino = normalizarTexto(this.terminoBusqueda.trim());
        if (!termino) {
            return this.listMedicos;
        }
        return this.listMedicos.filter((medico) =>
            [
                medico.nombres,
                medico.apellidos,
                medico.numeroDocumento,
                medico.registroProfesional,
                medico.telefono,
                medico.especializacion?.nombre
            ].some((valor) => normalizarTexto(String(valor ?? '')).includes(termino))
        );
    }

    get medicoPaginados(): Medico[] {
        const inicio = (this.paginaActual - 1) * this.registrosPorPagina;
        return this.medicoFiltrados.slice(inicio, inicio + this.registrosPorPagina);
    }

    get totalPaginas(): number {
        return Math.ceil(this.medicoFiltrados.length / this.registrosPorPagina);
    }

    get paginas(): number[] {
        return Array.from({ length: this.totalPaginas }, (_, indice) => indice + 1);
    }

    listar(): void {
        this.cargando = true;
        this.errorCarga = false;
        this.medicoService.listar().subscribe({
            next: (medicos) => {
                this.listMedicos = medicos;
                this.paginaActual = 1;
                this.cargando = false;
            },
            error: (error: unknown) => {
                console.error('Error al obtener los médicos:', error);
                this.errorCarga = true;
                this.cargando = false;
            }
        });
    }

    /** Carga el catálogo de especializaciones que alimenta el select del formulario. */
    cargarEspecializaciones(): void {
        this.especializacionService.listar().subscribe({
            next: (especializaciones) => (this.listEspecializaciones = especializaciones),
            error: (error: unknown) => console.error('Error al obtener las especializaciones:', error)
        });
    }

    actualizarBusqueda(event: Event): void {
        this.terminoBusqueda = (event.target as HTMLInputElement).value;
        this.paginaActual = 1;
    }

    cambiarPagina(pagina: number): void {
        if (pagina >= 1 && pagina <= this.totalPaginas) {
            this.paginaActual = pagina;
        }
    }

    nuevoMedico(modo: string): void {
        this.modoFormulario = modo;
        this.medicoSeleccionado = null;
        this.resetFormulario();
        this.openModal(modo);
    }

    abrirEdicion(medico: Medico): void {
        this.modoFormulario = 'E';
        this.medicoSeleccionado = medico;
        this.form.patchValue({
            tipoDocumento: medico.tipoDocumento ?? 'CC',
            numeroDocumento: medico.numeroDocumento ?? '',
            nombres: medico.nombres ?? '',
            apellidos: medico.apellidos ?? '',
            telefono: medico.telefono ?? '',
            registroProfesional: medico.registroProfesional ?? '',
            especializacionId: medico.especializacion?.id ?? null
        });
        this.openModal(this.modoFormulario);
    }

    async eliminar(medico: Medico): Promise<void> {
        const confirmacion = await Swal.fire({
            title: 'Eliminar médico',
            text: `¿Está seguro de eliminar al médico "${medico.nombres} ${medico.apellidos}"?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar',
            confirmButtonColor: '#d9534f'
        });

        if (!confirmacion.isConfirmed || medico.id === undefined) {
            return;
        }

        this.medicoService.eliminar(medico.id).subscribe({
            next: (respuesta) => {
                Swal.fire('Médico eliminado', respuesta.message, 'success');
                this.listar();
            },
            error: (error: unknown) => {
                Swal.fire('No se pudo eliminar', extraerMensajeError(error), 'error');
            }
        });
    }

    guardar(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        const valores = this.form.getRawValue();
        const payload: MedicoRq = {
            id: this.medicoSeleccionado?.id ?? null,
            tipoDocumento: String(valores['tipoDocumento']),
            numeroDocumento: String(valores['numeroDocumento']).trim(),
            nombres: String(valores['nombres']).trim(),
            apellidos: String(valores['apellidos']).trim(),
            telefono: this.valorOpcional(valores['telefono']),
            registroProfesional: String(valores['registroProfesional']).trim(),
            especializacionId: Number(valores['especializacionId'])
        };

        const petition = this.modoFormulario === 'C'
            ? this.medicoService.guardar(payload)
            : this.medicoService.actualizar(payload);

        this.guardando = true;
        petition.subscribe({
            next: (respuesta) => {
                this.guardando = false;
                Swal.fire('Operación exitosa', respuesta.message, 'success');
                this.closeModal();
                this.listar();
            },
            error: (error: unknown) => {
                this.guardando = false;
                Swal.fire('No se pudo guardar', extraerMensajeError(error), 'error');
            }
        });
    }

    private valorOpcional(valor: unknown): string | null {
        const texto = String(valor ?? '').trim();
        return texto.length > 0 ? texto : null;
    }

    resetFormulario(): void {
        this.form.reset({ tipoDocumento: 'CC', telefono: '' });
        this.form.markAsPristine();
        this.form.markAsUntouched();
    }

    closeModal(): void {
        if (this.modalInstance) {
            this.modalInstance.hide();
        }
        this.resetFormulario();
    }

    openModal(modo: string): void {
        this.titleModal = modo === 'C' ? 'Crear Médico' : 'Editar Médico';
        this.titleBoton = modo === 'C' ? 'Guardar Médico' : 'Actualizar Médico';
        this.modoFormulario = modo;
        const modalElement = document.getElementById('modalCrearMedico');
        if (modalElement) {
            this.modalInstance ??= new Modal(modalElement);
            this.modalInstance.show();
        }
    }
}