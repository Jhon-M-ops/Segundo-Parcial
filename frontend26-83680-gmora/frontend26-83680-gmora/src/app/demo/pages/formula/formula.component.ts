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
import { Cita } from 'src/app/models/cita';
import { Cliente } from 'src/app/models/cliente';
import { FormulaDetalle, FormulaResumen, FormulaRq } from 'src/app/models/formula';
import { Mascota } from 'src/app/models/mascota';
import { Medicamento } from 'src/app/models/medicamento';
import { Medico } from 'src/app/models/medico';
import { ClienteService } from '../usuario/service/cliente.service';
import { MascotaService } from '../mascota/service/mascota.service';
import { MedicoService } from '../medico/service/medico.service';
import { MedicamentoService } from '../medicamento/service/medicamento.service';
import { CitaService } from '../cita/service/cita.service';
import { FormulaService } from './service/formula.service';

/**
 * Módulo de gestión de fórmulas médicas.
 *
 * - Listado: paginación, filtros y ordenamiento resueltos en el servidor.
 * - Creación/edición: formulario reactivo con combos dependientes
 *   (Cliente → Mascotas del cliente → Citas de la mascota).
 * - Ver: detalle completo (el listado solo muestra un resumen).
 */
@Component({
    selector: 'app-formula',
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './formula.component.html',
    styleUrl: './formula.component.scss'
})
export class FormulaComponent implements OnInit, OnDestroy {

    // ----- Listado (paginación, orden y filtros) -----
    formulas: FormulaResumen[] = [];
    totalElementos: number = 0;
    totalPaginas: number = 0;
    page: number = 0;
    size: number = 3;
    /** Orden inicial exigido: fecha de creación descendente. */
    sortBy: string = 'fechaCreacion';
    sortDir: 'asc' | 'desc' = 'desc';
    cargando: boolean = false;
    errorCarga: boolean = false;
    mensajeError: string = '';
    guardando: boolean = false;
    /** Verdadero mientras se consulta un detalle o se inactiva una fórmula. */
    procesando: boolean = false;
    /** Verdadero mientras se cargan las mascotas del cliente elegido. */
    cargandoMascotas: boolean = false;
    /** Verdadero mientras se cargan las citas de la mascota elegida. */
    cargandoCitas: boolean = false;
    filtrosForm!: FormGroup;

    // ----- Catálogos y combos dependientes -----
    clientes: Cliente[] = [];
    mascotas: Mascota[] = [];
    citas: Cita[] = [];
    medicos: Medico[] = [];
    medicamentos: Medicamento[] = [];
    readonly unidadesDosis: string[] = ['mg', 'ml', 'g', 'tableta', 'capsula', 'gotas', 'UI'];

    // ----- Formulario crear / editar y detalle -----
    form!: FormGroup;
    modoFormulario: 'C' | 'E' = 'C';
    formulaEditando: FormulaDetalle | null = null;
    formulaDetalle: FormulaDetalle | null = null;
    /** Fecha (yyyy-MM-dd) de la cita elegida: la emisión no puede ser anterior (regla 4). */
    fechaMinima: string | null = null;

    private modalForm: Modal | null = null;
    private modalVer: Modal | null = null;
    private readonly destroy$ = new Subject<void>();

    constructor(
        private readonly fb: FormBuilder,
        private readonly formulaService: FormulaService,
        private readonly clienteService: ClienteService,
        private readonly mascotaService: MascotaService,
        private readonly citaService: CitaService,
        private readonly medicoService: MedicoService,
        private readonly medicamentoService: MedicamentoService
    ) {}

    /** Inicializa formularios, catálogos y carga la primera página. */
    ngOnInit(): void {
        this.filtrosForm = this.fb.group({
            texto: [''],
            medicoId: [null as number | null],
            medicamentoId: [null as number | null],
            activo: [''],
            fechaDesde: [null as string | null],
            fechaHasta: [null as string | null]
        });
        this.inicializarFormulario();
        this.enlazarCombosDependientes();
        this.cargarCatalogos();
        this.listar();

        // Al cambiar cualquier filtro se espera 400 ms y se vuelve a la primera página.
        this.filtrosForm.valueChanges
            .pipe(debounceTime(400), takeUntil(this.destroy$))
            .subscribe(() => {
                this.page = 0;
                this.listar();
            });
    }

    /** Libera las suscripciones al destruir el componente. */
    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    // =====================================================================
    // 1.1 LISTADO
    // =====================================================================

    /** Consulta en el backend la página actual con los filtros y el orden vigentes. */
    listar(): void {
        this.cargando = true;
        this.errorCarga = false;
        this.formulaService.listar({
            page: this.page,
            size: this.size,
            sortBy: this.sortBy,
            sortDir: this.sortDir,
            ...this.filtrosForm.value
        }).subscribe({
            next: (respuesta) => {
                this.formulas = respuesta.content;
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
     * Ordena por la columna indicada; si ya estaba ordenada invierte la dirección.
     *
     * @param campo clave de la columna (ver lista blanca del backend).
     */
    ordenarPor(campo: string): void {
        this.sortDir = this.sortBy === campo && this.sortDir === 'asc' ? 'desc' : 'asc';
        this.sortBy = campo;
        this.page = 0;
        this.listar();
    }

    /**
     * Devuelve la clase del ícono de orden de una columna.
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
     * @param pagina número de página destino.
     */
    irAPagina(pagina: number): void {
        if (pagina >= 0 && pagina < this.totalPaginas) {
            this.page = pagina;
            this.listar();
        }
    }

    /**
     * Cambia el número de registros por página.
     *
     * @param evento evento change del select.
     */
    cambiarTamano(evento: Event): void {
        this.size = Number((evento.target as HTMLSelectElement).value);
        this.page = 0;
        this.listar();
    }

    /** Limpia todos los filtros y recarga el listado. */
    limpiarFiltros(): void {
        this.filtrosForm.reset({ texto: '', activo: '' });
    }

    // =====================================================================
    // 1.2 CREACIÓN Y 1.3 EDICIÓN (formulario)
    // =====================================================================

    /** Crea el formulario reactivo con sus validaciones. */
    inicializarFormulario(): void {
        this.form = this.fb.group({
            clienteId: [null as number | null, Validators.required], // solo UI: no se envía
            mascotaId: [{ value: null as number | null, disabled: true }, Validators.required],
            citaId: [{ value: null as number | null, disabled: true }, Validators.required],
            medicoId: [null as number | null, Validators.required],
            medicamentoId: [null as number | null, Validators.required],
            fechaEmision: [this.hoy(), [Validators.required, this.fechaNoAnteriorACita()]],
            dosis: [null as number | null, [Validators.required, Validators.min(0.01)]],
            unidadDosis: ['', Validators.required],
            frecuencia: ['', [Validators.required, Validators.maxLength(100)]],
            duracionDias: [null as number | null, [Validators.required, Validators.min(1), Validators.max(365)]],
            indicaciones: ['', [Validators.required, Validators.maxLength(1000)]]
        });
    }

    /** Acceso corto a los controles del formulario desde la plantilla. */
    get f(): { [key: string]: AbstractControl } {
        return this.form.controls;
    }

    /**
     * Validador de la regla 4: la fecha de emisión no puede ser anterior a la
     * fecha de la consulta. El backend vuelve a validarla.
     */
    private fechaNoAnteriorACita(): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            const fecha = control.value as string | null;
            return fecha && this.fechaMinima && fecha < this.fechaMinima ? { anteriorACita: true } : null;
        };
    }

    /**
     * Enlaza los combos dependientes: Cliente → Mascotas del cliente → Citas de la mascota.
     * Cada combo hijo se vacía y deshabilita hasta que el padre tenga valor.
     */
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
                            // Una cita cancelada no tuvo consulta: no admite fórmula.
                            this.cargandoCitas = false;
                            this.citas = citas.filter((c) => c.estado?.toUpperCase() !== 'CANCELADA');
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
                if (cita?.medico?.id) {
                    // Se sugiere el veterinario de la cita; el usuario puede cambiarlo.
                    this.form.patchValue({ medicoId: cita.medico.id }, { emitEvent: false });
                }
                this.form.get('fechaEmision')!.updateValueAndValidity();
            });
    }

    /**
     * Deja un combo en blanco y deshabilitado sin disparar eventos en cascada.
     *
     * @param nombre nombre del control.
     */
    private vaciarCombo(nombre: string): void {
        const control = this.form.get(nombre)!;
        control.reset(null, { emitEvent: false });
        control.disable({ emitEvent: false });
    }

    /** Abre el modal para registrar una nueva fórmula. */
    nuevaFormula(): void {
        this.modoFormulario = 'C';
        this.formulaEditando = null;
        this.fechaMinima = null;
        this.mascotas = [];
        this.citas = [];
        this.form.reset({ fechaEmision: this.hoy() }, { emitEvent: false });
        this.vaciarCombo('mascotaId');
        this.vaciarCombo('citaId');
        this.form.get('clienteId')!.enable({ emitEvent: false });
        this.form.get('medicoId')!.enable({ emitEvent: false });
        this.abrirModal('modalFormula');
    }

    /**
     * Abre el modal de edición. Se consulta el DETALLE porque el listado solo
     * trae un resumen. Mascota, cita y veterinario quedan bloqueados.
     *
     * @param fila fórmula seleccionada en el listado.
     */
    editar(fila: FormulaResumen): void {
        this.procesando = true;
        this.formulaService.consultar(fila.formulaId).subscribe({
            next: (detalle) => {
                this.procesando = false;
                this.modoFormulario = 'E';
                this.formulaEditando = detalle;
                this.fechaMinima = detalle.citaFechaHora.substring(0, 10);
                this.form.patchValue({
                    mascotaId: detalle.mascotaId,
                    citaId: detalle.citaId,
                    medicoId: detalle.medicoId,
                    medicamentoId: detalle.medicamentoId,
                    fechaEmision: detalle.fechaEmision,
                    dosis: detalle.dosis,
                    unidadDosis: detalle.unidadDosis,
                    frecuencia: detalle.frecuencia,
                    duracionDias: detalle.duracionDias,
                    indicaciones: detalle.indicaciones
                }, { emitEvent: false });
                // Deshabilitados: quedan fuera de la validación pero getRawValue() los incluye.
                ['clienteId', 'mascotaId', 'citaId', 'medicoId']
                    .forEach((nombre) => this.form.get(nombre)!.disable({ emitEvent: false }));
                this.abrirModal('modalFormula');
            },
            error: (error: unknown) => {
                this.procesando = false;
                mostrarError('No se pudo cargar la fórmula', error);
            }
        });
    }

    /** Valida el formulario y envía la creación (POST) o la edición (PUT). */
    guardar(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        const valores = this.form.getRawValue();
        const solicitud: FormulaRq = {
            mascotaId: Number(valores.mascotaId),
            citaId: Number(valores.citaId),
            medicoId: Number(valores.medicoId),
            medicamentoId: Number(valores.medicamentoId),
            fechaEmision: valores.fechaEmision,
            dosis: Number(valores.dosis),
            unidadDosis: valores.unidadDosis,
            frecuencia: String(valores.frecuencia).trim(),
            duracionDias: Number(valores.duracionDias),
            indicaciones: String(valores.indicaciones).trim()
        };

        const peticion = this.modoFormulario === 'C'
            ? this.formulaService.guardar(solicitud)
            : this.formulaService.actualizar(this.formulaEditando!.formulaId, solicitud);

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
    // 1.3 VISUALIZACIÓN E INACTIVACIÓN
    // =====================================================================

    /**
     * Abre el modal con el detalle completo de la fórmula.
     *
     * @param fila fórmula seleccionada en el listado.
     */
    ver(fila: FormulaResumen): void {
        this.procesando = true;
        this.formulaService.consultar(fila.formulaId).subscribe({
            next: (detalle) => {
                this.procesando = false;
                this.formulaDetalle = detalle;
                this.abrirModal('modalVerFormula');
            },
            error: (error: unknown) => {
                this.procesando = false;
                mostrarError('No se pudo cargar la fórmula', error);
            }
        });
    }

    /**
     * Inactiva una fórmula tras confirmación. Es el reemplazo de la
     * eliminación física (regla 8).
     *
     * @param fila fórmula seleccionada en el listado.
     */
    async inactivar(fila: FormulaResumen): Promise<void> {
        const confirmacion = await Swal.fire({
            title: 'Inactivar fórmula',
            text: 'La fórmula quedará inactiva y ya no podrá editarse. ¿Desea continuar?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Sí, inactivar',
            cancelButtonText: 'Cancelar',
            confirmButtonColor: '#d9534f'
        });
        if (!confirmacion.isConfirmed) {
            return;
        }
        this.procesando = true;
        this.formulaService.inactivar(fila.formulaId).subscribe({
            next: (respuesta) => {
                this.procesando = false;
                Swal.fire('Fórmula inactivada', respuesta.message, 'success');
                this.listar();
            },
            error: (error: unknown) => {
                this.procesando = false;
                mostrarError('No se pudo inactivar', error);
            }
        });
    }

    // =====================================================================
    // Utilidades
    // =====================================================================

    /** Carga los catálogos que alimentan los combos y filtros. */
    private cargarCatalogos(): void {
        this.clienteService.listar().subscribe({
            next: (clientes) => (this.clientes = clientes),
            error: (error: unknown) => console.error('Error al obtener los clientes:', error)
        });
        this.medicoService.listar().subscribe({
            next: (medicos) => (this.medicos = medicos),
            error: (error: unknown) => console.error('Error al obtener los veterinarios:', error)
        });
        this.medicamentoService.listar().subscribe({
            next: (medicamentos) => (this.medicamentos = medicamentos),
            error: (error: unknown) => console.error('Error al obtener los medicamentos:', error)
        });
    }

    /** Devuelve la fecha de hoy en formato yyyy-MM-dd (hora local). */
    private hoy(): string {
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
        if (id === 'modalFormula') {
            this.modalForm ??= new Modal(elemento);
            this.modalForm.show();
        } else {
            this.modalVer ??= new Modal(elemento);
            this.modalVer.show();
        }
    }
}
