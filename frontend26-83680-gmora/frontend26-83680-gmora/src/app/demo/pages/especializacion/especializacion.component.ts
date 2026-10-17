import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from 'bootstrap';
import Swal from 'sweetalert2';
import { extraerMensajeError } from 'src/app/helpers/error.helper';
import { normalizarTexto } from 'src/app/helpers/text.helper';
import { Especializacion, EspecializacionRq } from 'src/app/models/especializacion';
import { EspecializacionService } from './service/especializacion.service';

@Component({
    selector: 'app-especializacion',
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './especializacion.component.html',
    styleUrl: './especializacion.component.scss'
})
export class EspecializacionComponent {

    // Variables para el modal.
    modalInstance: Modal | null = null;
    titleModal: string = '';
    modoFormulario: string = '';
    titleBoton: string = '';

    // Variables para la paginación y búsqueda en la datatable.
    listEspecializaciones: Especializacion[] = [];
    terminoBusqueda: string = '';
    paginaActual: number = 1;
    readonly registrosPorPagina: number = 10;
    cargando: boolean = false;
    errorCarga: boolean = false;
    guardando: boolean = false;

    form!: FormGroup;
    especializacionSeleccionada: Especializacion | null = null;

    constructor(
        private readonly especializacionService: EspecializacionService,
        private readonly formBuilder: FormBuilder
    ) {
        this.inicializarFormulario();
        this.listar();
    }

    inicializarFormulario(): void {
        this.form = this.formBuilder.group({
            nombre: ['', [Validators.required, Validators.maxLength(100)]],
            codigoEspecializacion: ['', [Validators.required, Validators.maxLength(10)]],
            descripcion: ['']
        });
    }

    get f(): { [key: string]: AbstractControl } {
        return this.form.controls;
    }

    get especializacionFiltradas(): Especializacion[] {
        const termino = normalizarTexto(this.terminoBusqueda.trim());
        if (!termino) {
            return this.listEspecializaciones;
        }
        return this.listEspecializaciones.filter((especializacion) =>
            [especializacion.nombre, especializacion.codigoEspecializacion, especializacion.descripcion].some((valor) =>
                normalizarTexto(String(valor ?? '')).includes(termino)
            )
        );
    }

    get especializacionPaginadas(): Especializacion[] {
        const inicio = (this.paginaActual - 1) * this.registrosPorPagina;
        return this.especializacionFiltradas.slice(inicio, inicio + this.registrosPorPagina);
    }

    get totalPaginas(): number {
        return Math.ceil(this.especializacionFiltradas.length / this.registrosPorPagina);
    }

    get paginas(): number[] {
        return Array.from({ length: this.totalPaginas }, (_, indice) => indice + 1);
    }

    listar(): void {
        this.cargando = true;
        this.errorCarga = false;
        this.especializacionService.listar().subscribe({
            next: (especializaciones) => {
                this.listEspecializaciones = especializaciones;
                this.paginaActual = 1;
                this.cargando = false;
            },
            error: (error: unknown) => {
                console.error('Error al obtener las especializaciones:', error);
                this.errorCarga = true;
                this.cargando = false;
            }
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

    nuevaEspecializacion(modo: string): void {
        this.modoFormulario = modo;
        this.especializacionSeleccionada = null;
        this.resetFormulario();
        this.openModal(modo);
    }

    abrirEdicion(especializacion: Especializacion): void {
        this.modoFormulario = 'E';
        this.especializacionSeleccionada = especializacion;
        this.form.patchValue({
            nombre: especializacion.nombre ?? '',
            codigoEspecializacion: especializacion.codigoEspecializacion ?? '',
            descripcion: especializacion.descripcion ?? ''
        });
        this.openModal(this.modoFormulario);
    }

    async eliminar(especializacion: Especializacion): Promise<void> {
        const confirmacion = await Swal.fire({
            title: 'Eliminar especialización',
            text: `¿Está seguro de eliminar la especialización "${especializacion.nombre}"?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar',
            confirmButtonColor: '#d9534f'
        });

        if (!confirmacion.isConfirmed || especializacion.id === undefined) {
            return;
        }

        this.especializacionService.eliminar(especializacion.id).subscribe({
            next: (respuesta) => {
                Swal.fire('Especialización eliminada', respuesta.message, 'success');
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
        const payload: EspecializacionRq = {
            id: this.especializacionSeleccionada?.id ?? null,
            nombre: String(valores['nombre']).trim(),
            codigoEspecializacion: String(valores['codigoEspecializacion']).trim().toUpperCase(),
            descripcion: String(valores['descripcion'] ?? '').trim()
        };

        const petition = this.modoFormulario === 'C'
            ? this.especializacionService.guardar(payload)
            : this.especializacionService.actualizar(payload);

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

    resetFormulario(): void {
        this.form.reset();
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
        this.titleModal = modo === 'C' ? 'Crear Especialización' : 'Editar Especialización';
        this.titleBoton = modo === 'C' ? 'Guardar Especialización' : 'Actualizar Especialización';
        this.modoFormulario = modo;
        const modalElement = document.getElementById('modalCrearEspecializacion');
        if (modalElement) {
            this.modalInstance ??= new Modal(modalElement);
            this.modalInstance.show();
        }
    }
}