# Estudio de paridad visual legacy ↔ v2

Fecha: 2026-10-26
Rama de referencia: `feature/analysis-fixes`

## Alcance y método

Comparado `legacy/src/main/webapp` (XHTML/JSF, Bootstrap 5.3.5 local, Bootstrap Icons 1.10.5,
Font Awesome kit, flatpickr, Google Charts) con `frontend/src` (React 18 + Vite, Bootstrap 5.3.8
npm, Bootstrap Icons + Font Awesome, `theme.css`).

Revisadas: plantillas, sidebars, login/home, listas (student/test/instance), detalle de test,
detalle de instancia, modales, paginación, mensajes y flujo del alumno.

## Lo que ya es igual

- Bootstrap 5.3 en ambos; misma rejilla, utilidades, tipografía del sistema y componentes base.
- `HomePage.tsx` y `LoginPage.tsx` son port casi literal de `home.xhtml` y `login.xhtml`
  (misma estructura, clases y proporciones).
- Tablas de resultados e instancias ya usan `table-borderless table-hover`, mismas columnas y
  menú de 3 puntos.
- Mismo patrón de badge de estado, paginación inferior y tarjetas en el flujo del alumno.
- Ambas apps cargan Font Awesome + Bootstrap Icons (aunque no los mismos iconos).

## Divergencias

| Área | Legacy | v2 | Impacto |
|---|---|---|---|
| Idioma | Español (`messages.properties`, latin-1) | Inglés hardcodeado | Muy alto |
| Color primario | Bootstrap por defecto (`#0d6efd`) | `#183153` en `theme.css` | Muy alto |
| Shell | `container-lg`, sidebar **primary redondeada** con logo, offcanvas <lg, modal de logout | Sidebar **dark fija 250px**, sin logo ni offcanvas, logout directo | Muy alto |
| Navegación | Tests / Alumnos / Clases / Centros / Usuarios; Centros y Usuarios solo admin; sin Dashboard; login → Welcome | Dashboard / Tests / Instances / Students / Groups / Centers / Users; Centros visible a todos; login → Dashboard | Alto |
| Cabeceras | Breadcrumb (`bi-house-fill / …`) + `h2` + botón “+” icono | `h3` + botón texto “+ New” | Alto |
| Filtros | Panel desplegable (filtros, Reset), chips activos, selector de orden, lupa | Búsqueda inline y selects sueltos; sin panel ni chips en varias | Alto |
| Tablas | Borderless + hover, columna `#`, sin columnas extra | `table-striped`, columnas Type/Questions, enums crudos (`MALE`, `CLOSED`) | Medio-alto |
| Badges | `bg-*-subtle border rounded-pill p-2` con `color` del enum | Sólidos (`bg-success`, `bg-warning`) | Medio |
| Paginación | “Página X de Y” + flechas `btn-light` | Números + « » + “N total” | Medio |
| Modales | `modal fade` + JS Bootstrap, `btn-light`, confirmaciones dedicadas | `Modal` propio sin fade, `btn-secondary`, `window.confirm` | Alto |
| Feedback | Toasts fijos abajo-derecha con icono y sombra | `alert` inline arriba | Medio |
| Gráficas | Google Charts (pie + columnas, 400px) | Barras de progreso Bootstrap | Medio |
| Detalle test | Badges subtle, tarjetas colapsables, acciones icono | Badges sólidos, tarjetas simples, edición en modal | Alto |
| Flujo alumno | Una pregunta a la vez, imágenes grandes, Anterior/Siguiente, modal de fin | Todas las preguntas en lista, radios, `confirm()`, PIN duplicado (Home + `PinEntry`) | Muy alto |
| Fechas | LocalDate crudo / `yyyy-MM-dd HH:mm` | `dd/MM/yyyy` / `yyyy-MM-dd HH:mm` | Bajo-medio |
| Título/favicon | `CTTest - <página>` | `CTTest` fijo + `vite.svg` | Bajo |
| Dependencias | CDN (BS JS, icons, FA kit, flatpickr, charts) | npm, sin BS JS | Medio (offline/CSP) |
| Responsive | Offcanvas en móvil | Sidebar fija que no colapsa | Alto |
| Assets | `Logo_URJC_white.png` | Falta en `frontend/public` | Bajo |

## Causa

El port se hizo **página a página**, sin sistema de diseño ni decisión previa de idioma/tema.
De ahí: inglés, navy, sidebar distinta, iconos FA mezclados con BI y simplificación del flujo
del alumno.

## Opciones

- **A. Legacy canónico (recomendado):** ajustar v2 (shell, componentes, idioma). Riesgo funcional
  bajo, trabajo cosmético alto.
- **B. v2 canónico:** rehacer XHTML/CSS y flujo del alumno en JSF. Caro y frágil.
- **C. Híbrido:** misma identidad visual y componentes, permitiendo mejoras de v2 (Dashboard,
  accesibilidad, sin CDN). Recomendado si “iguales” significa misma apariencia y estructura,
  no píxel perfecto.

## Cambios si A/C

- `frontend/src/theme.css`: alinear primario/tipografía o portar el navy al legacy (una sola
  fuente de verdad).
- `components/Layout.tsx`: `container-lg`, sidebar primary redondeada con logo blanco,
  offcanvas <lg (estado React, sin BS JS), modal de logout, menú y roles como legacy.
- `index.html`: título por página y favicon.
- Nuevos: `PageHeader` (breadcrumb + h2 + acción), `FilterPanel` (panel + chips + orden),
  `StatusBadge` (subtle/pill/borde), `ConfirmModal`, `Toast`.
- Tablas: wrapper `table-responsive` + `table-borderless table-hover`, columna `#`, revisar
  columnas extra.
- `Modal`: fade, `btn-light`, `aria-*`; sustituir `confirm()`.
- `Pagination`: “Página X de Y” + chevrons.
- i18n: catálogo `es` (o traducir legacy a inglés, según decisión).
- Fechas: helper único (`dd/MM/yyyy`, `dd/MM/yyyy HH:mm`).
- `Tests.tsx`: pestañas “Mi centro / Explorar”; `TestDetail`: tarjetas colapsables;
  `TestInstanceDetail`: charts o alternativa acordada.
- `StudentTestFlow.tsx`: rediseño al patrón legacy.
- `frontend/public`: `Logo_URJC_white.png`.

## Riesgos

- **i18n:** cientos de literales; codificación legacy latin-1 (usar UTF-8 en v2).
- **Color primario:** afecta contraste y todos los componentes; validar AA.
- **Offcanvas/modales:** si se importa BS JS hay desincronización con React; hacerlo con estado.
- **Paridad píxel-perfecto:** inviable por markup JSF; fijar criterio (tokens + estructura +
  capturas).
- **Regresiones** al tocar Layout/rutas y al cambiar el flujo del alumno (es un rediseño,
  no un ajuste).
- **Mantenimiento doble** mientras convivan ambas apps; acordar congelación/retirada del legacy.
- **CDN** del legacy y del FA kit (red/CSP) frente a npm en v2.
- **Datos/fechas:** decidir formato canónico; ojo con zona horaria.
- **Accesibilidad:** badges subtle con contraste justo.

## Plan propuesto (1 persona)

0. Decidir canónico, idioma y criterio; congelar legacy — 1-2 h.
1. Fundamentos: tokens, i18n, fechas, título/favicon — 1-2 d.
2. Shell: Layout, offcanvas, logout, menú/roles, logo — 1-2 d.
3. Componentes compartidos: PageHeader, FilterPanel, StatusBadge, Pagination, Modal, Toast — 2-3 d.
4. Listas: Centers, Students, Groups, Users, Tests, Instances — 2-3 d.
5. Detalles: TestDetail, TestInstanceDetail — 2 d.
6. Flujo alumno: PIN/inicio/preguntas/fin — 3-5 d.
7. QA visual lado a lado (375/768/1280), teclado, consola, responsive — 2 d.
8. Limpieza y documentación.

Total ≈ 2-3 semanas (solo admin ≈ 1 semana).

## Decisiones pendientes

1. ¿Idioma canónico: español o inglés?
2. ¿Paridad pixel-perfect o misma identidad/estructura (opción C)?
3. ¿Se mantiene el Dashboard o se elimina para igualar?
4. ¿Entra el flujo del alumno en esta iteración?
5. ¿Se aplica `dd/MM/yyyy` también al legacy?
6. ¿Habrá retirada del legacy a corto plazo?

## Decisiones acordadas (2026-10-26)

1. Idioma canónico: **español**. Catálogo i18n en v2; legacy queda como referencia.
2. Criterio de paridad: **opción C** (misma identidad y estructura, mejoras de v2 permitidas:
   accesibilidad, npm sin CDN).
3. **Se mantiene el Dashboard** en v2 como valor añadido.
4. El **flujo del alumno entra completo** en esta iteración.
5. Fechas: **`dd/MM/yyyy` y `dd/MM/yyyy HH:mm` en ambas apps**. Es el último cambio
   permitido en el legacy.
6. **Retirada del legacy a corto plazo**: queda congelado salvo bugfix crítico y el ajuste
   de fechas.

Rama de trabajo: `feature/ui-parity`.
