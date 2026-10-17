import { CommonModule, formatDate } from '@angular/common';
import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from 'bootstrap';
import Swal from 'sweetalert2';
import { extraerMensajeError } from 'src/app/helpers/error.helper';
import { normalizarTexto } from 'src/app/helpers/text.helper';
import { Cliente } from 'src/app/models/cliente';
import { Mascota, MascotaRq } from 'src/app/models/mascota';
import { Raza } from 'src/app/models/raza';
import { ClienteService } from '../usuario/service/cliente.service';
import { RazaService } from '../raza/service/raza.service';
import { MascotaService } from './service/mascota.service';

@Component({
    selector: 'app-mascota',
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './mascota.component.html',
    styleUrl: './mascota.component.scss'
})
export class MascotaComponent {

    // Variables para el modal.
    modalInstance: Modal | null = null;
    titleModal: string = '';
    modoFormulario: string = '';
    titleBoton: string = '';

    // Variables para la paginación y búsqueda en la datatable.
    listMascotas: Mascota[] = [];
    listClientes: Cliente[] = [];
    listRazas: Raza[] = [];
    terminoBusqueda: string = '';
    paginaActual: number = 1;
    readonly registrosPorPagina: number = 10;
    cargando: boolean = false;
    errorCarga: boolean = false;
    guardando: boolean = false;

    form!: FormGroup;
    mascotaSelected: Mascota | null = null;

    constructor(
        private readonly mascotaService: MascotaService,
        private readonly clienteService: ClienteService,
        private readonly razaService: RazaService,
        private readonly formBuilder: FormBuilder
    ) {
        this.inicializarFormulario();
        this.listar();
        this.cargarCatalogos();
    }

    inicializarFormulario(): void {
        this.form = this.formBuilder.group({
            nombreMascota: ['', Validators.required],
            razaId: [null as number | null, Validators.required],
            edad: [null as number | null, [Validators.required, Validators.min(0)]],
            clienteId: [null as number | null, Validators.required]
        });
    }

    get f(): { [key: string]: AbstractControl } {
        return this.form.controls;
    }

    get mascotasFiltradas(): Mascota[] {
        const termino = normalizarTexto(this.terminoBusqueda.trim());
        if (!termino) {
            return this.listMascotas;
        }
        return this.listMascotas.filter((mascota) => {
            const valores = [
                mascota.nombreMascota,
                mascota.raza?.especie,
                mascota.raza?.nombre,
                mascota.edad,
                `${mascota.cliente?.nombres ?? ''} ${mascota.cliente?.apellidos ?? ''}`,
                mascota.fechaRegistro ? formatDate(mascota.fechaRegistro, 'yyyy-MM-dd HH:mm', 'en-US') : ''
            ];
            return valores.some((valor) => normalizarTexto(String(valor ?? '')).includes(termino));
        });
    }

    get mascotasPaginadas(): Mascota[] {
        const inicio = (this.paginaActual - 1) * this.registrosPorPagina;
        return this.mascotasFiltradas.slice(inicio, inicio + this.registrosPorPagina);
    }

    get totalPaginas(): number {
        return Math.ceil(this.mascotasFiltradas.length / this.registrosPorPagina);
    }

    get paginas(): number[] {
        return Array.from({ length: this.totalPaginas }, (_, indice) => indice + 1);
    }

    listar(): void {
        this.cargando = true;
        this.errorCarga = false;
        this.mascotaService.listar().subscribe({
            next: (mascotas) => {
                this.listMascotas = mascotas;
                this.paginaActual = 1;
                this.cargando = false;
            },
            error: (error: unknown) => {
                console.error('Error al obtener las mascotas:', error);
                this.errorCarga = true;
                this.cargando = false;
            }
        });
    }

    /** Carga los catálogos de clientes y razas que alimentan los selects del formulario. */
    cargarCatalogos(): void {
        this.clienteService.listar().subscribe({
            next: (clientes) => (this.listClientes = clientes),
            error: (error: unknown) => console.error('Error al obtener los clientes:', error)
        });
        this.razaService.listar().subscribe({
            next: (razas) => (this.listRazas = razas),
            error: (error: unknown) => console.error('Error al obtener las razas:', error)
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

    nuevaMascota(modo: string): void {
        this.modoFormulario = modo;
        this.mascotaSelected = null;
        this.resetFormulario();
        this.openModal(modo);
    }

    abrirEdicion(mascota: Mascota): void {
        this.modoFormulario = 'E';
        this.mascotaSelected = mascota;
        this.form.patchValue({
            nombreMascota: mascota.nombreMascota ?? '',
            razaId: mascota.raza?.razaId ?? null,
            edad: mascota.edad ?? null,
            clienteId: mascota.cliente?.clienteId ?? null
        });
        this.openModal(this.modoFormulario);
    }

    async eliminar(mascota: Mascota): Promise<void> {
        const confirmacion = await Swal.fire({
            title: 'Eliminar mascota',
            text: `¿Está seguro de eliminar la mascota "${mascota.nombreMascota}"?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar',
            confirmButtonColor: '#d9534f'
        });

        if (!confirmacion.isConfirmed || mascota.mascotaId === undefined) {
            return;
        }

        this.mascotaService.eliminar(mascota.mascotaId).subscribe({
            next: (respuesta) => {
                Swal.fire('Mascota eliminada', respuesta.message, 'success');
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
        const payload: MascotaRq = {
            mascotaId: this.mascotaSelected?.mascotaId ?? null,
            nombreMascota: String(valores['nombreMascota']).trim(),
            edad: Number(valores['edad']),
            razaId: Number(valores['razaId']),
            clienteId: Number(valores['clienteId'])
        };

        const petition = this.modoFormulario === 'C'
            ? this.mascotaService.guardar(payload)
            : this.mascotaService.actualizar(payload);

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
        this.titleModal = modo === 'C' ? 'Crear Mascota' : 'Editar Mascota';
        this.titleBoton = modo === 'C' ? 'Guardar Mascota' : 'Actualizar Mascota';
        this.modoFormulario = modo;
        const modalElement = document.getElementById('modalCrearMascota');
        if (modalElement) {
            this.modalInstance ??= new Modal(modalElement);
            this.modalInstance.show();
        }
    }
}