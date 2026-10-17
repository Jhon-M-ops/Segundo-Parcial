import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import {
    AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule,
    ValidationErrors, ValidatorFn, Validators
} from '@angular/forms';
import { Modal } from 'bootstrap';
import { Subject, debounceTime, takeUntil } from 'rxjs';
import Swal from 'sweetalert2';
import { extraerMensajeError, mostrarError } from 'src/app/helpers/error.helper';
import {
    CitaDetalle, CitaResumen, CitaRq, ESTADOS_CITA, ESTADOS_INICIALES_CITA,
    EstadoCita, TRANSICIONES_CITA
} from 'src/app/models/cita';
import { Cliente } from 'src/app/models/cliente';
import { Mascota } from 'src/app/models/mascota';
import { Medico } from 'src/app/models/medico';
import { ClienteService } from '../usuario/service/cliente.service';
import { MascotaService } from '../mascota/service/mascota.service';
import { MedicoService } from '../medico/service/medico.service';
import { CitaService } from './service/cita.service';

/**
 * Módulo de gestión de citas médicas.
 *
 * - Listado: paginación, filtros y orden resueltos en el servidor
 *   (orden inicial: fecha y hora descendente).
 * - Creación: combos dependientes Cliente → Mascotas del cliente.
 * - Edición: los campos editables dependen del estado de la cita.
 * - Cambios de estado: confirmar, atender, cancelar y reactivar son
 *   operaciones explícitas. El backend es quien valida las reglas críticas.
 */
@Component({
    selector: 'app-cita',
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './cita.component.html',
    styleUrl: './cita.component.scss'
})
export class CitaComponent implements OnInit, OnDestroy {

    // ----- Listado -----
    citas: CitaResumen[] = [];
    totalElementos: number = 0;
    totalPaginas: number = 0;
    page: number = 0;
    size: number = 10;
    /** Orden inicial exigido: fecha y hora descendente. */
    sortBy: string = 'fechaHora';
    sortDir: 'asc' | 'desc' = 'desc';
    cargando: boolean = false;
    errorCarga: boolean = false;
    mensajeError: string = '';
    guardando: boolean = false;
    /** Verdadero mientras se consulta un detalle o se ejecuta un cambio de estado. */
    procesando: boolean = false;
    /** Verdadero mientras se cargan las mascotas del cliente elegido. */
    cargandoMascotas: boolean = false;
    filtrosForm!: FormGroup;

    // ----- Catálogos -----
    clientes: Cliente[] = [];
    mascotas: Mascota[] = [];
    medicos: Medico[] = [];
    readonly estados = ESTADOS_CITA;
    readonly estadosIniciales = ESTADOS_CITA.filter((e) => ESTADOS_INICIALES_CITA.includes(e.valor));

    // ----- Formulario crear / editar y detalle -----
    form!: FormGroup;
    modoFormulario: 'C' | 'E' = 'C';
    citaEditando: CitaDetalle | null = null;
    citaDetalle: CitaDetalle | null = null;
    /** Fecha mínima seleccionable (hoy, regla 6). */
    readonly hoy: string = this.fechaDeHoy();
    /** Fecha y hora originales al editar: si no cambian, no se exige que sean futuras. */
    private fechaHoraOriginal: string | null = null;

    private modalForm: Modal | null = null;
    private modalVer: Modal | null = null;
    private readonly destroy$ = new Subject<void>();

    constructor(
        private readonly fb: FormBuilder,
        private readonly citaService: CitaService,
        private readonly clienteService: ClienteService,
        private readonly mascotaService: MascotaService,
        private readonly medicoService: MedicoService
    ) {}

    /** Inicializa formularios, catálogos y carga la primera página. */
    ngOnInit(): void {
        this.filtrosForm = this.fb.group({
            texto: [''],
            medicoId: [null as number | null],
            estado: [''],
            fechaDesde: [null as string | null],
            fechaHasta: [null as string | null]
        });
        this.inicializarFormulario();
        this.enlazarComboCliente();
        this.cargarCatalogos();
        this.listar();

        this.filtrosForm.valueChanges
            .pipe(debounceTime(400), takeUntil(this.destroy$))
            .subscribe(() => {
                this.page = 0;
                this.listar();
            });
    }

    /** Libera las suscripciones. */
    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    // =====================================================================
    // 2.1 LISTADO
    // =====================================================================

    /** Consulta la página actual con los filtros y el orden vigentes. */
    listar(): void {
        this.cargando = true;
        this.errorCarga = false;
        this.citaService.listar({
            page: this.page,
            size: this.size,
            sortBy: this.sortBy,
            sortDir: this.sortDir,
            ...this.filtrosForm.value
        }).subscribe({
            next: (respuesta) => {
                this.citas = respuesta.content;
                this.totalElementos = respuesta.totalElements;
                this.totalPaginas = respuesta.totalPages;
                this.cargando = false;
            },
            error: (error: unknown) => {
                this.mensajeError = extraerMensajeError(error);
                this.errorCarga = true;
                this.cargando = false;
            }
        });
    }

    /**
     * Ordena por la columna; si ya estaba ordenada invierte la dirección.
     *
     * @param campo clave de la columna.
     */
    ordenarPor(campo: string): void {
        this.sortDir = this.sortBy === campo && this.sortDir === 'asc' ? 'desc' : 'asc';
        this.sortBy = campo;
        this.page = 0;
        this.listar();
    }

    /**
     * Clase del ícono de orden de una columna.
     *
     * @param campo clave de la columna.
     */
    iconoOrden(campo: string): string {
        if (this.sortBy !== campo) {
            return 'fa-sort text-muted';
        }
        return this.sortDir === 'asc' ? 'fa-sort-up' : 'fa-sort-down';
    }

    /**
     * Navega a una página (base 0).
     *
     * @param pagina página destino.
     */
    irAPagina(pagina: number): void {
        if (pagina >= 0 && pagina < this.totalPaginas) {
            this.page = pagina;
            this.listar();
        }
    }

    /**
     * Cambia los registros por página.
     *
     * @param evento evento change del select.
     */
    cambiarTamano(evento: Event): void {
        this.size = Number((evento.target as HTMLSelectElement).value);
        this.page = 0;
        this.listar();
    }

    /** Limpia los filtros y recarga. */
    limpiarFiltros(): void {
        this.filtrosForm.reset({ texto: '', estado: '' });
    }

    // =====================================================================
    // Presentación y reglas de UI por estado (el backend es quien valida)
    // =====================================================================

    /**
     * Texto legible de un estado.
     *
     * @param estado estado en mayúsculas.
     */
    etiquetaEstado(estado: string): string {
        return this.estados.find((e) => e.valor === estado)?.etiqueta ?? estado;
    }

    /**
     * Clase de Bootstrap para el badge de un estado.
     *
     * @param estado estado en mayúsculas.
     */
    claseEstado(estado: string): string {
        const clases: Record<string, string> = {
            PENDIENTE: 'bg-warning text-dark',
            CONFIRMADA: 'bg-info text-dark',
            PROGRAMADA: 'bg-primary',
            ATENDIDA: 'bg-success',
            CANCELADA: 'bg-danger'
        };
        return clases[estado] ?? 'bg-secondary';
    }

    /**
     * Indica si desde un estado se puede pasar a otro.
     *
     * @param estado  estado actual.
     * @param destino estado destino.
     */
    puedePasarA(estado: string, destino: EstadoCita): boolean {
        return TRANSICIONES_CITA[estado as EstadoCita]?.includes(destino) ?? false;
    }

    /**
     * Una cita se edita solo si está Pendiente, Programada o Confirmada.
     *
     * @param estado estado actual.
     */
    puedeEditar(estado: string): boolean {
        return ['PENDIENTE', 'PROGRAMADA', 'CONFIRMADA'].includes(estado);
    }

    // =====================================================================
    // 2.2 CREACIÓN Y 2.3 EDICIÓN
    // =====================================================================

    /** Crea el formulario reactivo con sus validaciones. */
    inicializarFormulario(): void {
        this.form = this.fb.group({
            clienteId: [null as number | null, Validators.required],
            mascotaId: [{ value: null as number | null, disabled: true }, Validators.required],
            medicoId: [null as number | null, Validators.required],
            fecha: ['', Validators.required],
            hora: ['', Validators.required],
            motivo: ['', [Validators.required, Validators.maxLength(255)]],
            estado: ['PENDIENTE', Validators.required]
        }, { validators: this.fechaHoraNoPasada() });
    }

    /** Acceso corto a los controles desde la plantilla. */
    get f(): { [key: string]: AbstractControl } {
        return this.form.controls;
    }

    /**
     * Validador de grupo de la regla 6: fecha y hora no pueden estar en el
     * pasado. Al editar, si no cambiaron, no se exige (se puede corregir solo
     * el motivo de una cita de hoy cuya hora ya pasó).
     */
    private fechaHoraNoPasada(): ValidatorFn {
        return (grupo: AbstractControl): ValidationErrors | null => {
            const fecha = grupo.get('fecha')?.value as string;
            const hora = grupo.get('hora')?.value as string;
            if (!fecha || !hora) {
                return null;
            }
            const actual = `${fecha}T${hora}`;
            if (this.modoFormulario === 'E' && this.fechaHoraOriginal === actual) {
                return null;
            }
            return new Date(actual).getTime() < Date.now() ? { fechaHoraPasada: true } : null;
        };
    }

    /** Combo dependiente: al elegir un cliente se cargan solo sus mascotas. */
    private enlazarComboCliente(): void {
        this.form.get('clienteId')!.valueChanges
            .pipe(takeUntil(this.destroy$))
            .subscribe((clienteId: number | null) => {
                const mascota = this.form.get('mascotaId')!;
                mascota.reset(null, { emitEvent: false });
                mascota.disable({ emitEvent: false });
                this.mascotas = [];
                if (clienteId) {
                    this.cargandoMascotas = true;
                    this.mascotaService.listarPorCliente(Number(clienteId)).subscribe({
                        next: (mascotas) => {
                            this.mascotas = mascotas;
                            this.cargandoMascotas = false;
                            mascota.enable({ emitEvent: false });
                        },
                        error: (error: unknown) => {
                            this.cargandoMascotas = false;
                            mostrarError('No se pudieron cargar las mascotas', error);
                        }
                    });
                }
            });
    }

    /** Abre el modal para registrar una cita. */
    nuevaCita(): void {
        this.modoFormulario = 'C';
        this.citaEditando = null;
        this.fechaHoraOriginal = null;
        this.mascotas = [];
        Object.keys(this.form.controls).forEach((c) => this.form.get(c)!.enable({ emitEvent: false }));
        this.form.reset({ estado: 'PENDIENTE', fecha: '', hora: '', motivo: '' }, { emitEvent: false });
        this.form.get('mascotaId')!.disable({ emitEvent: false });
        this.abrirModal('modalCita');
    }

    /**
     * Abre el modal de edición. Qué campos quedan habilitados depende del estado:
     * Pendiente/Programada → fecha, hora, veterinario y motivo;
     * Confirmada → solo el motivo. Cliente, mascota y estado nunca se editan aquí.
     *
     * @param fila cita seleccionada en el listado.
     */
    editar(fila: CitaResumen): void {
        this.procesando = true;
        this.citaService.consultar(fila.citaId).subscribe({
            next: (detalle) => {
                this.procesando = false;
                this.modoFormulario = 'E';
                this.citaEditando = detalle;
                const fecha = detalle.fechaHora.substring(0, 10);
                const hora = detalle.fechaHora.substring(11, 16);
                this.fechaHoraOriginal = `${fecha}T${hora}`;

                Object.keys(this.form.controls).forEach((c) => this.form.get(c)!.enable({ emitEvent: false }));
                this.form.patchValue({
                    clienteId: detalle.clienteId,
                    mascotaId: detalle.mascotaId,
                    medicoId: detalle.medicoId,
                    fecha, hora,
                    motivo: detalle.motivo,
                    estado: detalle.estado
                }, { emitEvent: false });

                const bloqueados = ['clienteId', 'mascotaId', 'estado'];
                if (detalle.estado === 'CONFIRMADA') {
                    bloqueados.push('medicoId', 'fecha', 'hora');
                }
                bloqueados.forEach((c) => this.form.get(c)!.disable({ emitEvent: false }));
                this.form.updateValueAndValidity();
                this.abrirModal('modalCita');
            },
            error: (error: unknown) => {
                this.procesando = false;
                mostrarError('No se pudo cargar la cita', error);
            }
        });
    }

    /** Valida el formulario y envía la creación (POST) o la edición (PUT). */
    guardar(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }
        const v = this.form.getRawValue();
        const solicitud: CitaRq = {
            clienteId: Number(v.clienteId),
            mascotaId: Number(v.mascotaId),
            medicoId: Number(v.medicoId),
            fecha: v.fecha,
            hora: v.hora,
            motivo: String(v.motivo).trim(),
            estado: v.estado
        };
        const peticion = this.modoFormulario === 'C'
            ? this.citaService.guardar(solicitud)
            : this.citaService.actualizar(this.citaEditando!.citaId, solicitud);

        this.guardando = true;
        peticion.subscribe({
            next: (respuesta) => {
                this.guardando = false;
                Swal.fire('Operación exitosa', respuesta.message, 'success');
                this.cerrarFormulario();
                this.listar();
            },
            error: (error: unknown) => {
                this.guardando = false;
                mostrarError('No se pudo guardar', error);
            }
        });
    }

    /** Cierra el modal de crear/editar. */
    cerrarFormulario(): void {
        this.modalForm?.hide();
    }

    // =====================================================================
    // Ver y cambios de estado
    // =====================================================================

    /**
     * Abre el modal con el detalle completo de la cita.
     *
     * @param fila cita seleccionada.
     */
    ver(fila: CitaResumen): void {
        this.procesando = true;
        this.citaService.consultar(fila.citaId).subscribe({
            next: (detalle) => {
                this.procesando = false;
                this.citaDetalle = detalle;
                this.abrirModal('modalVerCita');
            },
            error: (error: unknown) => {
                this.procesando = false;
                mostrarError('No se pudo cargar la cita', error);
            }
        });
    }

    /**
     * Pide confirmación y cambia el estado de la cita (confirmar, atender, cancelar).
     *
     * @param fila    cita seleccionada.
     * @param destino estado destino.
     */
    async cambiarEstado(fila: CitaResumen, destino: EstadoCita): Promise<void> {
        const etiqueta = this.etiquetaEstado(destino);
        const confirmacion = await Swal.fire({
            title: `${etiqueta} cita`,
            text: destino === 'CANCELADA'
                ? 'La cita quedará cancelada y no podrá modificarse (solo reactivarse). ¿Desea continuar?'
                : `La cita pasará a estado ${etiqueta}. ¿Desea continuar?`,
            icon: destino === 'CANCELADA' ? 'warning' : 'question',
            showCancelButton: true,
            confirmButtonText: 'Sí, continuar',
            cancelButtonText: 'Volver'
        });
        if (!confirmacion.isConfirmed) {
            return;
        }
        this.procesando = true;
        this.citaService.cambiarEstado(fila.citaId, destino).subscribe({
            next: (respuesta) => {
                this.procesando = false;
                Swal.fire('Listo', respuesta.message, 'success');
                this.listar();
            },
            error: (error: unknown) => {
                this.procesando = false;
                mostrarError('No se pudo cambiar el estado', error);
            }
        });
    }

    /**
     * Reactiva una cita cancelada (operación explícita).
     *
     * @param fila cita seleccionada.
     */
    async reactivar(fila: CitaResumen): Promise<void> {
        const confirmacion = await Swal.fire({
            title: 'Reactivar cita',
            text: 'La cita volverá a estado Pendiente y se revalidará la fecha y la disponibilidad. ¿Continuar?',
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: 'Sí, reactivar',
            cancelButtonText: 'Volver'
        });
        if (!confirmacion.isConfirmed) {
            return;
        }
        this.procesando = true;
        this.citaService.reactivar(fila.citaId).subscribe({
            next: (respuesta) => {
                this.procesando = false;
                Swal.fire('Listo', respuesta.message, 'success');
                this.listar();
            },
            error: (error: unknown) => {
                this.procesando = false;
                mostrarError('No se pudo reactivar', error);
            }
        });
    }

    // =====================================================================
    // Utilidades
    // =====================================================================

    /** Carga los catálogos de clientes y veterinarios. */
    private cargarCatalogos(): void {
        this.clienteService.listar().subscribe({
            next: (clientes) => (this.clientes = clientes),
            error: (error: unknown) => console.error('Error al obtener los clientes:', error)
        });
        this.medicoService.listar().subscribe({
            next: (medicos) => (this.medicos = medicos),
            error: (error: unknown) => console.error('Error al obtener los veterinarios:', error)
        });
    }

    /** Fecha de hoy (hora local) en formato yyyy-MM-dd. */
    private fechaDeHoy(): string {
        const ahora = new Date();
        const mes = String(ahora.getMonth() + 1).padStart(2, '0');
        const dia = String(ahora.getDate()).padStart(2, '0');
        return `${ahora.getFullYear()}-${mes}-${dia}`;
    }

    /**
     * Abre un modal de Bootstrap creando la instancia la primera vez.
     *
     * @param id id del elemento HTML del modal.
     */
    private abrirModal(id: string): void {
        const elemento = document.getElementById(id);
        if (!elemento) {
            return;
        }
        if (id === 'modalCita') {
            this.modalForm ??= new Modal(elemento);
            this.modalForm.show();
        } else {
            this.modalVer ??= new Modal(elemento);
            this.modalVer.show();
        }
    }
}
