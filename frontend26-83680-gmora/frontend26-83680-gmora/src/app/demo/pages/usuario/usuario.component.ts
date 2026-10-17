import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from 'bootstrap';
import Swal from 'sweetalert2';
import { extraerMensajeError } from 'src/app/helpers/error.helper';
import { normalizarTexto } from 'src/app/helpers/text.helper';
import { Cliente, ClienteRq } from 'src/app/models/cliente';
import { ClienteService } from './service/cliente.service';

@Component({
    selector: 'app-cliente',
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './usuario.component.html',
    styleUrl: './usuario.component.scss'
})
export class ClienteComponent {

    // Variables para el modal.
    modalInstance: Modal | null = null;
    titleModal: string = '';
    modoFormulario: string = '';
    titleBoton: string = '';

    // Variables para la paginación y búsqueda en la datatable.
    listClientes: Cliente[] = [];
    terminoBusqueda: string = '';
    paginaActual: number = 1;
    readonly registrosPorPagina: number = 10;
    cargando: boolean = false;
    errorCarga: boolean = false;
    guardando: boolean = false;

    form!: FormGroup;
    clienteSeleccionado: Cliente | null = null;

    constructor(
        private readonly clienteService: ClienteService,
        private readonly formBuilder: FormBuilder
    ) {
        this.inicializarFormulario();
        this.listar();
    }

    inicializarFormulario(): void {
        this.form = this.formBuilder.group({
            tipoDocumento: ['CC', Validators.required],
            numeroDocumento: ['', Validators.required],
            nombres: ['', Validators.required],
            apellidos: ['', Validators.required],
            fechaNacimiento: ['', Validators.required],
            genero: [''],
            telefono: [''],
            direccion: [''],
            activo: [true]
        });
    }

    get f(): { [key: string]: AbstractControl } {
        return this.form.controls;
    }

    get clientesFiltrados(): Cliente[] {
        const termino = normalizarTexto(this.terminoBusqueda.trim());
        if (!termino) {
            return this.listClientes;
        }
        return this.listClientes.filter((cliente) =>
            [
                cliente.nombres,
                cliente.apellidos,
                cliente.tipoDocumento,
                cliente.numeroDocumento,
                cliente.telefono,
                cliente.direccion,
                cliente.activo ? 'activo' : 'inactivo'
            ].some((valor) => normalizarTexto(String(valor ?? '')).includes(termino))
        );
    }

    get clientesPaginados(): Cliente[] {
        const inicio = (this.paginaActual - 1) * this.registrosPorPagina;
        return this.clientesFiltrados.slice(inicio, inicio + this.registrosPorPagina);
    }

    get totalPaginas(): number {
        return Math.ceil(this.clientesFiltrados.length / this.registrosPorPagina);
    }

    get paginas(): number[] {
        return Array.from({ length: this.totalPaginas }, (_, indice) => indice + 1);
    }

    listar(): void {
        this.cargando = true;
        this.errorCarga = false;
        this.clienteService.listar().subscribe({
            next: (clientes) => {
                this.listClientes = clientes;
                this.paginaActual = 1;
                this.cargando = false;
            },
            error: (error: unknown) => {
                console.error('Error al obtener los clientes:', error);
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

    nuevoCliente(modo: string): void {
        this.modoFormulario = modo;
        this.clienteSeleccionado = null;
        this.resetFormulario();
        this.openModal(modo);
    }

    abrirEdicion(cliente: Cliente): void {
        this.modoFormulario = 'E';
        this.clienteSeleccionado = cliente;
        this.form.patchValue({
            tipoDocumento: cliente.tipoDocumento,
            numeroDocumento: cliente.numeroDocumento,
            nombres: cliente.nombres,
            apellidos: cliente.apellidos,
            fechaNacimiento: cliente.fechaNacimiento,
            genero: cliente.genero ?? '',
            telefono: cliente.telefono ?? '',
            direccion: cliente.direccion ?? '',
            activo: cliente.activo
        });
        this.openModal(this.modoFormulario);
    }

    async eliminar(cliente: Cliente): Promise<void> {
        const confirmacion = await Swal.fire({
            title: 'Eliminar cliente',
            text: `¿Está seguro de eliminar al cliente "${cliente.nombres} ${cliente.apellidos}"?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar',
            confirmButtonColor: '#d9534f'
        });

        if (!confirmacion.isConfirmed || cliente.clienteId === null) {
            return;
        }

        this.clienteService.eliminar(cliente.clienteId).subscribe({
            next: (respuesta) => {
                Swal.fire('Cliente eliminado', respuesta.message, 'success');
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
        const payload: ClienteRq = {
            clienteId: this.clienteSeleccionado?.clienteId ?? null,
            tipoDocumento: String(valores['tipoDocumento']),
            numeroDocumento: String(valores['numeroDocumento']).trim(),
            nombres: String(valores['nombres']).trim(),
            apellidos: String(valores['apellidos']).trim(),
            fechaNacimiento: String(valores['fechaNacimiento']),
            genero: this.valorOpcional(valores['genero']),
            telefono: this.valorOpcional(valores['telefono']),
            direccion: this.valorOpcional(valores['direccion']),
            activo: Boolean(valores['activo'])
        };

        const petition = this.modoFormulario === 'C'
            ? this.clienteService.guardar(payload)
            : this.clienteService.actualizar(payload);

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
        this.form.reset({ tipoDocumento: 'CC', genero: '', telefono: '', direccion: '', activo: true });
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
        this.titleModal = modo === 'C' ? 'Crear Cliente' : 'Editar Cliente';
        this.titleBoton = modo === 'C' ? 'Guardar Cliente' : 'Actualizar Cliente';
        this.modoFormulario = modo;
        const modalElement = document.getElementById('modalCrearCliente');
        if (modalElement) {
            this.modalInstance ??= new Modal(modalElement);
            this.modalInstance.show();
        }
    }
}