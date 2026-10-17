import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from 'bootstrap';
import Swal from 'sweetalert2';
import { extraerMensajeError } from 'src/app/helpers/error.helper';
import { normalizarTexto } from 'src/app/helpers/text.helper';
import { Medicamento, MedicamentoRq } from 'src/app/models/medicamento';
import { MedicamentoService } from './service/medicamento.service';

@Component({
    selector: 'app-medicamento',
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './medicamento.component.html',
    styleUrl: './medicamento.component.scss'
})
export class MedicamentoComponent {

    // Variables para el modal.
    modalInstance: Modal | null = null;
    titleModal: string = '';
    modoFormulario: string = '';
    titleBoton: string = '';

    // Variables para la paginación y búsqueda en la datatable.
    listMedicamentos: Medicamento[] = [];
    terminoBusqueda: string = '';
    soloVencidos: boolean = false;
    paginaActual: number = 1;
    readonly registrosPorPagina: number = 10;
    cargando: boolean = false;
    errorCarga: boolean = false;
    guardando: boolean = false;

    form!: FormGroup;
    medicamentoSeleccionado: Medicamento | null = null;

    constructor(
        private readonly medicamentoService: MedicamentoService,
        private readonly formBuilder: FormBuilder
    ) {
        this.inicializarFormulario();
        this.listar();
    }

    inicializarFormulario(): void {
        // fecha_compra y fecha_vence son NOT NULL en la tabla medicamento.
        this.form = this.formBuilder.group({
            nombre: ['', Validators.required],
            presentacion: [''],
            descripcion: [''],
            fechaCompra: ['', Validators.required],
            fechaVence: ['', Validators.required]
        });
    }

    get f(): { [key: string]: AbstractControl } {
        return this.form.controls;
    }

    /** Marca como vencidos los medicamentos cuya fecha de vencimiento ya pasó. */
    estaVencido(medicamento: Medicamento): boolean {
        if (!medicamento.fechaVence) {
            return false;
        }
        return new Date(medicamento.fechaVence).getTime() < Date.now();
    }

    get medicamentoFiltrados(): Medicamento[] {
        const termino = normalizarTexto(this.terminoBusqueda.trim());
        return this.listMedicamentos.filter((medicamento) => {
            if (this.soloVencidos && !this.estaVencido(medicamento)) {
                return false;
            }
            if (!termino) {
                return true;
            }
            return [medicamento.nombre, medicamento.presentacion, medicamento.descripcion].some((valor) =>
                normalizarTexto(String(valor ?? '')).includes(termino)
            );
        });
    }

    get medicamentoPaginados(): Medicamento[] {
        const inicio = (this.paginaActual - 1) * this.registrosPorPagina;
        return this.medicamentoFiltrados.slice(inicio, inicio + this.registrosPorPagina);
    }

    get totalPaginas(): number {
        return Math.ceil(this.medicamentoFiltrados.length / this.registrosPorPagina);
    }

    get paginas(): number[] {
        return Array.from({ length: this.totalPaginas }, (_, indice) => indice + 1);
    }

    listar(): void {
        this.cargando = true;
        this.errorCarga = false;
        this.medicamentoService.listar().subscribe({
            next: (medicamentos) => {
                this.listMedicamentos = medicamentos;
                this.paginaActual = 1;
                this.cargando = false;
            },
            error: (error: unknown) => {
                console.error('Error al obtener los medicamentos:', error);
                this.errorCarga = true;
                this.cargando = false;
            }
        });
    }

    actualizarBusqueda(event: Event): void {
        this.terminoBusqueda = (event.target as HTMLInputElement).value;
        this.paginaActual = 1;
    }

    alternarSoloVencidos(): void {
        this.soloVencidos = !this.soloVencidos;
        this.paginaActual = 1;
    }

    cambiarPagina(pagina: number): void {
        if (pagina >= 1 && pagina <= this.totalPaginas) {
            this.paginaActual = pagina;
        }
    }

    nuevoMedicamento(modo: string): void {
        this.modoFormulario = modo;
        this.medicamentoSeleccionado = null;
        this.resetFormulario();
        this.openModal(modo);
    }

    abrirEdicion(medicamento: Medicamento): void {
        this.modoFormulario = 'E';
        this.medicamentoSeleccionado = medicamento;
        this.form.patchValue({
            nombre: medicamento.nombre ?? '',
            presentacion: medicamento.presentacion ?? '',
            descripcion: medicamento.descripcion ?? '',
            fechaCompra: medicamento.fechaCompra ?? '',
            fechaVence: medicamento.fechaVence ?? ''
        });
        this.openModal(this.modoFormulario);
    }

    async eliminar(medicamento: Medicamento): Promise<void> {
        const confirmacion = await Swal.fire({
            title: 'Eliminar medicamento',
            text: `¿Está seguro de eliminar el medicamento "${medicamento.nombre}"?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar',
            confirmButtonColor: '#d9534f'
        });

        if (!confirmacion.isConfirmed || medicamento.id === undefined) {
            return;
        }

        this.medicamentoService.eliminar(medicamento.id).subscribe({
            next: (respuesta) => {
                Swal.fire('Medicamento eliminado', respuesta.message, 'success');
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
        const payload: MedicamentoRq = {
            id: this.medicamentoSeleccionado?.id ?? null,
            nombre: String(valores['nombre']).trim(),
            presentacion: String(valores['presentacion'] ?? '').trim(),
            descripcion: String(valores['descripcion'] ?? '').trim(),
            fechaCompra: String(valores['fechaCompra']),
            fechaVence: String(valores['fechaVence'])
        };

        const petition = this.modoFormulario === 'C'
            ? this.medicamentoService.guardar(payload)
            : this.medicamentoService.actualizar(payload);

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
        this.titleModal = modo === 'C' ? 'Crear Medicamento' : 'Editar Medicamento';
        this.titleBoton = modo === 'C' ? 'Guardar Medicamento' : 'Actualizar Medicamento';
        this.modoFormulario = modo;
        const modalElement = document.getElementById('modalCrearMedicamento');
        if (modalElement) {
            this.modalInstance ??= new Modal(modalElement);
            this.modalInstance.show();
        }
    }
}