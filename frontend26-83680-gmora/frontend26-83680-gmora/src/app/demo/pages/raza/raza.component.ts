import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from 'bootstrap';
import Swal from 'sweetalert2';
import { extraerMensajeError } from 'src/app/helpers/error.helper';
import { normalizarTexto } from 'src/app/helpers/text.helper';
import { Raza, RazaRq } from 'src/app/models/raza';
import { RazaService } from './service/raza.service';

@Component({
    selector: 'app-raza',
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './raza.component.html',
    styleUrl: './raza.component.scss'
})
export class RazaComponent {

    // Variables para el modal.
    modalInstance: Modal | null = null;
    titleModal: string = '';
    modoFormulario: string = '';
    titleBoton: string = '';

    // Variables para la paginación y búsqueda en la datatable.
    listRazas: Raza[] = [];
    terminoBusqueda: string = '';
    especieFiltro: string = '';
    paginaActual: number = 1;
    readonly registrosPorPagina: number = 10;
    cargando: boolean = false;
    errorCarga: boolean = false;
    guardando: boolean = false;

    form!: FormGroup;
    razaSeleccionada: Raza | null = null;

    constructor(private readonly razaService: RazaService, private readonly formBuilder: FormBuilder) {
        this.inicializarFormulario();
        this.listar();
    }

    inicializarFormulario(): void {
        this.form = this.formBuilder.group({
            nombre: ['', [Validators.required, Validators.maxLength(100)]],
            especie: ['', [Validators.required, Validators.maxLength(100)]]
        });
    }

    get f(): { [key: string]: AbstractControl } {
        return this.form.controls;
    }

    /** Especies disponibles en el catálogo, usadas para el filtro de la tabla. */
    get especies(): string[] {
        return Array.from(new Set(this.listRazas.map((raza) => raza.especie ?? '')))
            .filter((especie) => especie.length > 0)
            .sort((a, b) => a.localeCompare(b));
    }

    get razaFiltradas(): Raza[] {
        const especie = this.especieFiltro;
        const termino = normalizarTexto(this.terminoBusqueda.trim());

        return this.listRazas.filter((raza) => {
            const cumpleEspecie = !especie || raza.especie === especie;
            if (!cumpleEspecie) {
                return false;
            }
            if (!termino) {
                return true;
            }
            return [raza.nombre, raza.especie].some((valor) => normalizarTexto(String(valor ?? '')).includes(termino));
        });
    }

    get razaPaginadas(): Raza[] {
        const inicio = (this.paginaActual - 1) * this.registrosPorPagina;
        return this.razaFiltradas.slice(inicio, inicio + this.registrosPorPagina);
    }

    get totalPaginas(): number {
        return Math.ceil(this.razaFiltradas.length / this.registrosPorPagina);
    }

    get paginas(): number[] {
        return Array.from({ length: this.totalPaginas }, (_, indice) => indice + 1);
    }

    listar(): void {
        this.cargando = true;
        this.errorCarga = false;
        this.razaService.listar().subscribe({
            next: (razas) => {
                this.listRazas = razas;
                this.paginaActual = 1;
                this.cargando = false;
            },
            error: (error: unknown) => {
                console.error('Error al obtener las razas:', error);
                this.errorCarga = true;
                this.cargando = false;
            }
        });
    }

    actualizarBusqueda(event: Event): void {
        this.terminoBusqueda = (event.target as HTMLInputElement).value;
        this.paginaActual = 1;
    }

    actualizarFiltroEspecie(event: Event): void {
        this.especieFiltro = (event.target as HTMLSelectElement).value;
        this.paginaActual = 1;
    }

    cambiarPagina(pagina: number): void {
        if (pagina >= 1 && pagina <= this.totalPaginas) {
            this.paginaActual = pagina;
        }
    }

    nuevaRaza(modo: string): void {
        this.modoFormulario = modo;
        this.razaSeleccionada = null;
        this.resetFormulario();
        this.openModal(modo);
    }

    abrirEdicion(raza: Raza): void {
        this.modoFormulario = 'E';
        this.razaSeleccionada = raza;
        this.form.patchValue({
            nombre: raza.nombre ?? '',
            especie: raza.especie ?? ''
        });
        this.openModal(this.modoFormulario);
    }

    async eliminar(raza: Raza): Promise<void> {
        const confirmacion = await Swal.fire({
            title: 'Eliminar raza',
            text: `¿Está seguro de eliminar la raza "${raza.nombre}"?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar',
            confirmButtonColor: '#d9534f'
        });

        if (!confirmacion.isConfirmed || raza.razaId === undefined) {
            return;
        }

        this.razaService.eliminar(raza.razaId).subscribe({
            next: (respuesta) => {
                Swal.fire('Raza eliminada', respuesta.message, 'success');
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
        const payload: RazaRq = {
            razaId: this.razaSeleccionada?.razaId ?? null,
            nombre: String(valores['nombre']).trim(),
            especie: String(valores['especie']).trim()
        };

        const petition = this.modoFormulario === 'C'
            ? this.razaService.guardar(payload)
            : this.razaService.actualizar(payload);

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
        this.titleModal = modo === 'C' ? 'Crear Raza' : 'Editar Raza';
        this.titleBoton = modo === 'C' ? 'Guardar Raza' : 'Actualizar Raza';
        this.modoFormulario = modo;
        const modalElement = document.getElementById('modalCrearRaza');
        if (modalElement) {
            this.modalInstance ??= new Modal(modalElement);
            this.modalInstance.show();
        }
    }
}