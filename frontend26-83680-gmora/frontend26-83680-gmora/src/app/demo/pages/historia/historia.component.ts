import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import {
    AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators
} from '@angular/forms';
import { Modal } from 'bootstrap';
import { Subject, debounceTime, takeUntil } from 'rxjs';
import Swal from 'sweetalert2';
import { extraerMensajeError, mostrarError } from 'src/app/helpers/error.helper';
import { Cita } from 'src/app/models/cita';
import { Cliente } from 'src/app/models/cliente';
import {
    Anotacion, AnotacionRq, ESTADOS_HISTORIA, HistoriaDetalle, HistoriaResumen, HistoriaRq
} from 'src/app/models/historia';
import { Mascota } from 'src/app/models/mascota';
import { Medico } from 'src/app/models/medico';
import { ClienteService } from '../usuario/service/cliente.service';
import { MascotaService } from '../mascota/service/mascota.service';
import { MedicoService } from '../medico/service/medico.service';
import { CitaService } from '../cita/service/cita.service';
import { HistoriaService } from './service/historia.service';

/**
 * Módulo de historias clínicas y anotaciones.
 *
 * - Listado: paginación, filtros y orden en el servidor (fecha y hora descendente).
 * - Creación: combos dependientes Cliente → Mascotas → Citas atendidas de la mascota.
 * - Ver: detalle de la historia y su histórico de anotaciones en una tabla con
 *   filtros, orden por fecha, paginación y detalle de cada anotación.
 * - Anotaciones: se agregan a una historia abierta; la fecha la pone el servidor.
 */
@Component({
    selector: 'app-historia',
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './historia.component.html',
    styleUrl: './historia.component.scss'
})
export class HistoriaComponent implements OnInit, OnDestroy {

    // ----- Listado de historias -----
    historias: HistoriaResumen[] = [];
    totalElementos: number = 0;
    totalPaginas: number = 0;
    page: number = 0;
    size: number = 10;
    /** Orden inicial exigido: fecha y hora de la consulta descendente. */
    sortBy: string = 'fechaHora';
    sortDir: 'asc' | 'desc' = 'desc';
    cargando: boolean = false;
    errorCarga: boolean = false;
    mensajeError: string = '';
    guardando: boolean = false;
    procesando: boolean = false;
    cargandoMascotas: boolean = false;
    cargandoCitas: boolean = false;
    filtrosForm!: FormGroup;

    // ----- Catálogos -----
    clientes: Cliente[] = [];
    mascotas: Mascota[] = [];
    citas: Cita[] = [];
    medicos: Medico[] = [];
    readonly estados = ESTADOS_HISTORIA;

    // ----- Formulario de creación -----
    form!: FormGroup;
    /** Fecha (yyyy-MM-dd) de la cita elegida: la consulta no puede ser anterior. */
    fechaMinima: string | null = null;
    /** Límite superior del campo fecha y hora: ahora (no se admiten consultas futuras). */
    maxFechaHora: string = '';

    // ----- Vista "Ver": detalle + anotaciones -----
    historiaDetalle: HistoriaDetalle | null = null;
    anotaciones: Anotacion[] = [];
    anotTotalElementos: number = 0;
    anotTotalPaginas: number = 0;
    anotPage: number = 0;
    anotSize: number = 5;
    /** El histórico se muestra de la más reciente a la más antigua. */
    anotSortDir: 'asc' | 'desc' = 'desc';
    cargandoAnotaciones: boolean = false;
    anotFiltrosForm!: FormGroup;
    anotacionDetalle: Anotacion | null = null;
    mostrarFormAnotacion: boolean = false;
    guardandoAnotacion: boolean = false;
    anotForm!: FormGroup;

    private modalForm: Modal | null = null;
    private modalVer: Modal | null = null;
    private readonly destroy$ = new Subject<void>();

    constructor(
        private readonly fb: FormBuilder,
        private readonly historiaService: HistoriaService,
        private readonly clienteService: ClienteService,
        private readonly mascotaService: MascotaService,
        private readonly citaService: CitaService,
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
        this.anotFiltrosForm = this.fb.group({
            texto: [''],
            fechaDesde: [null as string | null],
            fechaHasta: [null as string | null]
        });
        this.inicializarFormulario();
        this.inicializarFormularioAnotacion();
        this.enlazarCombosDependientes();
        this.cargarCatalogos();
        this.listar();

        this.filtrosForm.valueChanges
            .pipe(debounceTime(400), takeUntil(this.destroy$))
            .subscribe(() => {
                this.page = 0;
                this.listar();
            });
        this.anotFiltrosForm.valueChanges
            .pipe(debounceTime(400), takeUntil(this.destroy$))
            .subscribe(() => {
                this.anotPage = 0;
                this.cargarAnotaciones();
            });
    }

    /** Libera las suscripciones. */
    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    // =====================================================================
    // 3.1 LISTADO DE HISTORIAS
    // =====================================================================

    /** Consulta la página actual con los filtros y el orden vigentes. */
    listar(): void {
        this.cargando = true;
        this.errorCarga = false;
        this.historiaService.listar({
            page: this.page,
            size: this.size,
            sortBy: this.sortBy,
            sortDir: this.sortDir,
            ...this.filtrosForm.value
        }).subscribe({
            next: (respuesta) => {
                this.historias = respuesta.content;
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
     * Navega a una página del listado (base 0).
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
     * Cambia los registros por página del listado.
     *
     * @param evento evento change del select.
     */
    cambiarTamano(evento: Event): void {
        this.size = Number((evento.target as HTMLSelectElement).value);
        this.page = 0;
        this.listar();
    }

    /** Limpia los filtros del listado. */
    limpiarFiltros(): void {
        this.filtrosForm.reset({ texto: '', estado: '' });
    }

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
        return estado === 'ABIERTA' ? 'bg-success' : 'bg-secondary';
    }

    // =====================================================================
    // 3.2 CREACIÓN DE HISTORIA CLÍNICA
    // =====================================================================

    /** Crea el formulario reactivo de la historia con sus validaciones. */
    private inicializarFormulario(): void {
        this.form = this.fb.group({
            clienteId: [null as number | null, Validators.required],
            mascotaId: [{ value: null as number | null, disabled: true }, Validators.required],
            citaId: [{ value: null as number | null, disabled: true }, Validators.required],
            medicoId: [null as number | null, Validators.required],
            fechaHora: ['', [Validators.required, (c: AbstractControl) => this.validarFechaHora(c)]],
            motivo: ['', [Validators.required, Validators.maxLength(255)]],
            diagnostico: ['', [Validators.required, Validators.maxLength(2000)]],
            observaciones: ['', Validators.maxLength(2000)]
        });
    }

    /** Acceso corto a los controles de la historia desde la plantilla. */
    get f(): { [key: string]: AbstractControl } {
        return this.form.controls;
    }

    /**
     * Valida que la consulta no sea futura ni anterior a la fecha de la cita.
     * El backend vuelve a validar estas reglas.
     *
     * @param control campo fecha y hora (formato yyyy-MM-ddTHH:mm).
     */
    private validarFechaHora(control: AbstractControl): ValidationErrors | null {
        const valor = control.value as string;
        if (!valor) {
            return null;
        }
        if (new Date(valor).getTime() > Date.now()) {
            return { futura: true };
        }
        if (this.fechaMinima && valor.substring(0, 10) < this.fechaMinima) {
            return { anteriorACita: true };
        }
        return null;
    }

    /** Combos dependientes: Cliente → Mascotas del cliente → Citas atendidas de la mascota. */
    private enlazarCombosDependientes(): void {
        this.form.get('clienteId')!.valueChanges
            .pipe(takeUntil(this.destroy$))
            .subscribe((clienteId: number | null) => {
                this.vaciarCombo('mascotaId');
                this.vaciarCombo('citaId');
                this.mascotas = [];
                this.citas = [];
                this.fechaMinima = null;
                if (clienteId) {
                    this.cargandoMascotas = true;
                    this.mascotaService.listarPorCliente(Number(clienteId)).subscribe({
                        next: (mascotas) => {
                            this.mascotas = mascotas;
                            this.cargandoMascotas = false;
                            this.form.get('mascotaId')!.enable({ emitEvent: false });
                        },
                        error: (error: unknown) => {
                            this.cargandoMascotas = false;
                            mostrarError('No se pudieron cargar las mascotas', error);
                        }
                    });
                }
            });

        this.form.get('mascotaId')!.valueChanges
            .pipe(takeUntil(this.destroy$))
            .subscribe((mascotaId: number | null) => {
                this.vaciarCombo('citaId');
                this.citas = [];
                this.fechaMinima = null;
                if (mascotaId) {
                    this.cargandoCitas = true;
                    this.citaService.listarPorMascota(Number(mascotaId)).subscribe({
                        next: (citas) => {
                            // Solo una cita atendida puede generar historia clínica.
                            this.citas = citas.filter((c) => c.estado?.toUpperCase() === 'ATENDIDA');
                            this.cargandoCitas = false;
                            this.form.get('citaId')!.enable({ emitEvent: false });
                        },
                        error: (error: unknown) => {
                            this.cargandoCitas = false;
                            mostrarError('No se pudieron cargar las citas', error);
                        }
                    });
                }
            });

        this.form.get('citaId')!.valueChanges
            .pipe(takeUntil(this.destroy$))
            .subscribe((citaId: number | null) => {
                const cita = this.citas.find((c) => c.citaId === Number(citaId));
                this.fechaMinima = cita ? cita.fechaHora.substring(0, 10) : null;
                if (cita) {
                    // Se precargan los datos de la cita; el usuario puede ajustarlos.
                    this.form.patchValue({
                        medicoId: cita.medico?.id ?? null,
                        motivo: (cita.motivo ?? '').substring(0, 255),
                        fechaHora: cita.fechaHora.substring(0, 16)
                    }, { emitEvent: false });
                }
                this.form.get('fechaHora')!.updateValueAndValidity();
            });
    }

    /**
     * Deja un combo vacío y deshabilitado sin disparar eventos en cascada.
     *
     * @param nombre nombre del control.
     */
    private vaciarCombo(nombre: string): void {
        const control = this.form.get(nombre)!;
        control.reset(null, { emitEvent: false });
        control.disable({ emitEvent: false });
    }

    /** Abre el modal para registrar una historia clínica. */
    nuevaHistoria(): void {
        this.fechaMinima = null;
        this.mascotas = [];
        this.citas = [];
        this.maxFechaHora = this.ahoraLocal();
        this.form.reset({ fechaHora: '', motivo: '', diagnostico: '', observaciones: '' }, { emitEvent: false });
        this.vaciarCombo('mascotaId');
        this.vaciarCombo('citaId');
        this.form.get('clienteId')!.enable({ emitEvent: false });
        this.abrirModal('modalHistoria');
    }

    /** Valida el formulario y envía la creación (POST). */
    guardar(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }
        const v = this.form.getRawValue();
        const solicitud: HistoriaRq = {
            clienteId: Number(v.clienteId),
            mascotaId: Number(v.mascotaId),
            citaId: Number(v.citaId),
            medicoId: Number(v.medicoId),
            fechaHora: v.fechaHora,
            motivo: String(v.motivo).trim(),
            diagnostico: String(v.diagnostico).trim(),
            observaciones: String(v.observaciones ?? '').trim()
        };
        this.guardando = true;
        this.historiaService.guardar(solicitud).subscribe({
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

    /** Cierra el modal de creación. */
    cerrarFormulario(): void {
        this.modalForm?.hide();
    }

    /**
     * Cierra una historia (ya no admitirá anotaciones) tras confirmación.
     *
     * @param fila historia seleccionada en el listado.
     */
    async cerrarHistoria(fila: HistoriaResumen): Promise<void> {
        const confirmacion = await Swal.fire({
            title: 'Cerrar historia clínica',
            text: 'Una historia cerrada ya no admite nuevas anotaciones. ¿Desea continuar?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Sí, cerrar',
            cancelButtonText: 'Volver'
        });
        if (!confirmacion.isConfirmed) {
            return;
        }
        this.procesando = true;
        this.historiaService.cerrar(fila.historiaId).subscribe({
            next: (respuesta) => {
                this.procesando = false;
                Swal.fire('Listo', respuesta.message, 'success');
                this.listar();
            },
            error: (error: unknown) => {
                this.procesando = false;
                mostrarError('No se pudo cerrar la historia', error);
            }
        });
    }

    // =====================================================================
    // 3.4 VISUALIZACIÓN: DETALLE + HISTÓRICO DE ANOTACIONES
    // =====================================================================

    /**
     * Abre el modal con el detalle de la historia y su histórico de anotaciones.
     *
     * @param fila historia seleccionada en el listado.
     */
    ver(fila: HistoriaResumen): void {
        this.procesando = true;
        this.historiaService.consultar(fila.historiaId).subscribe({
            next: (detalle) => {
                this.procesando = false;
                this.historiaDetalle = detalle;
                this.anotacionDetalle = null;
                this.mostrarFormAnotacion = false;
                this.anotPage = 0;
                this.anotSortDir = 'desc';
                this.anotFiltrosForm.reset({ texto: '' }, { emitEvent: false });
                this.cargarAnotaciones();
                this.abrirModal('modalVerHistoria');
            },
            error: (error: unknown) => {
                this.procesando = false;
                mostrarError('No se pudo cargar la historia', error);
            }
        });
    }

    /** Consulta la página actual de anotaciones con filtros y orden por fecha. */
    cargarAnotaciones(): void {
        if (!this.historiaDetalle) {
            return;
        }
        this.cargandoAnotaciones = true;
        this.historiaService.listarAnotaciones(this.historiaDetalle.historiaId, {
            page: this.anotPage,
            size: this.anotSize,
            sortDir: this.anotSortDir,
            ...this.anotFiltrosForm.value
        }).subscribe({
            next: (respuesta) => {
                this.anotaciones = respuesta.content;
                this.anotTotalElementos = respuesta.totalElements;
                this.anotTotalPaginas = respuesta.totalPages;
                this.cargandoAnotaciones = false;
            },
            error: (error: unknown) => {
                this.cargandoAnotaciones = false;
                mostrarError('No se pudieron cargar las anotaciones', error);
            }
        });
    }

    /** Invierte el orden por fecha del histórico (más reciente ↔ más antigua). */
    ordenarAnotaciones(): void {
        this.anotSortDir = this.anotSortDir === 'desc' ? 'asc' : 'desc';
        this.anotPage = 0;
        this.cargarAnotaciones();
    }

    /**
     * Navega a una página del histórico de anotaciones.
     *
     * @param pagina página destino (base 0).
     */
    irAPaginaAnotaciones(pagina: number): void {
        if (pagina >= 0 && pagina < this.anotTotalPaginas) {
            this.anotPage = pagina;
            this.cargarAnotaciones();
        }
    }

    /** Limpia los filtros del histórico de anotaciones. */
    limpiarFiltrosAnotaciones(): void {
        this.anotFiltrosForm.reset({ texto: '' });
    }

    /**
     * Consulta y muestra el detalle completo de una anotación.
     *
     * @param fila anotación seleccionada en la tabla.
     */
    verAnotacion(fila: Anotacion): void {
        if (!this.historiaDetalle) {
            return;
        }
        this.historiaService.consultarAnotacion(this.historiaDetalle.historiaId, fila.anotacionId).subscribe({
            next: (anotacion) => (this.anotacionDetalle = anotacion),
            error: (error: unknown) => mostrarError('No se pudo cargar la anotación', error)
        });
    }

    // =====================================================================
    // 3.3 ANOTACIONES
    // =====================================================================

    /** Crea el formulario reactivo de la anotación. */
    private inicializarFormularioAnotacion(): void {
        this.anotForm = this.fb.group({
            medicoId: [null as number | null, Validators.required],
            descripcion: ['', [Validators.required, Validators.maxLength(500)]],
            observacion: ['', Validators.maxLength(2000)],
            diagnosticoEvolucion: ['', [Validators.required, Validators.maxLength(2000)]]
        });
    }

    /** Acceso corto a los controles de la anotación desde la plantilla. */
    get fa(): { [key: string]: AbstractControl } {
        return this.anotForm.controls;
    }

    /** Muestra u oculta el formulario para agregar una anotación. */
    alternarFormularioAnotacion(): void {
        this.mostrarFormAnotacion = !this.mostrarFormAnotacion;
        if (this.mostrarFormAnotacion) {
            this.anotForm.reset({
                medicoId: this.historiaDetalle?.medicoId ?? null,
                descripcion: '', observacion: '', diagnosticoEvolucion: ''
            });
        }
    }

    /** Valida y envía la nueva anotación (POST); luego recarga el histórico. */
    guardarAnotacion(): void {
        if (!this.historiaDetalle) {
            return;
        }
        if (this.anotForm.invalid) {
            this.anotForm.markAllAsTouched();
            return;
        }
        const v = this.anotForm.getRawValue();
        const solicitud: AnotacionRq = {
            medicoId: Number(v.medicoId),
            descripcion: String(v.descripcion).trim(),
            observacion: String(v.observacion ?? '').trim(),
            diagnosticoEvolucion: String(v.diagnosticoEvolucion).trim()
        };
        this.guardandoAnotacion = true;
        this.historiaService.guardarAnotacion(this.historiaDetalle.historiaId, solicitud).subscribe({
            next: (respuesta) => {
                this.guardandoAnotacion = false;
                Swal.fire('Operación exitosa', respuesta.message, 'success');
                this.mostrarFormAnotacion = false;
                this.anotPage = 0;
                this.anotSortDir = 'desc';   // la nueva anotación queda de primera
                this.cargarAnotaciones();
            },
            error: (error: unknown) => {
                this.guardandoAnotacion = false;
                mostrarError('No se pudo guardar la anotación', error);
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

    /** Fecha y hora actuales (hora local) en formato yyyy-MM-ddTHH:mm. */
    private ahoraLocal(): string {
        const a = new Date();
        const dos = (n: number): string => String(n).padStart(2, '0');
        return `${a.getFullYear()}-${dos(a.getMonth() + 1)}-${dos(a.getDate())}T${dos(a.getHours())}:${dos(a.getMinutes())}`;
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
        if (id === 'modalHistoria') {
            this.modalForm ??= new Modal(elemento);
            this.modalForm.show();
        } else {
            this.modalVer ??= new Modal(elemento);
            this.modalVer.show();
        }
    }
}
