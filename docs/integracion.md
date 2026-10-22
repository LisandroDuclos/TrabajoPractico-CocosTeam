# Integración de las funcionalidades y frontend

Documento de trabajo de la rama `feature/sistema-integrado`. Plantea cómo unir las tres
funcionalidades obligatorias en un único programa con utilidad real, y cómo sumarle una
interfaz gráfica. Es una propuesta para discutir en el equipo, no una decisión cerrada.

## 1. Objetivo y reglas

Objetivo: que el sistema se use como lo usaría la organización. Primero se cargan la red, los
pedidos y los vehículos; después se consultan los pedidos, se arma la carga de un viaje y se
calcula su recorrido. Todo en un mismo programa.

Reglas para no salirnos de la consigna:

- **Los algoritmos no cambian.** `MergeSort`, `BusquedaBinaria`, `Dijkstra`, `MinHeap` y
  `SeleccionCargas` quedan como están en `main`, y sus pruebas siguen pasando sin tocarlas.
  La integración se hace en una capa nueva que los llama.
- **Cada funcionalidad resuelve sólo lo que pide la consigna.** La F3 elige la carga de *un*
  viaje y la F2 calcula la ruta *de un origen a un destino*. La consigna dice que la carga y el
  recorrido "se resolverán por separado", así que el sistema los encadena pero no los fusiona en
  un solo algoritmo.
- **Mismas restricciones de la materia en todas las capas, también en la interfaz.** Nada de
  `Arrays.sort`, `Collections.sort`, métodos de búsqueda de Java, streams, lambdas ni librerías
  externas. Cualquier orden o búsqueda que vea el usuario sale de nuestros algoritmos.
- **El frontend es un agregado.** La consigna no lo pide. La demo por consola (`Main`) se
  mantiene tal cual, y el informe tiene que dejar claro que la interfaz es una capa de
  presentación y no reemplaza nada de lo pedido.

## 2. Situación actual

Hoy las tres funcionalidades comparten proyecto, paquetes y `Main`, pero no comparten datos:

- El destino de `Pedido` es un `String` libre. En los datos de ejemplo, "Centro Sur" y "Centro
  Centro" tienen pedidos pero no existen en la red de la F2, y "Centro Puerto" está en la red
  pero no tiene pedidos.
- La capacidad del vehículo en la F3 es un `int` suelto: no existe la entidad `Vehiculo`, aunque
  la consigna la nombra entre las que administra el sistema.
- `Lote` no se relaciona con ninguna otra entidad.

## 3. Modelo integrado

Entidades que nombra la consigna: depósitos, centros, conexiones viales, vehículos, pedidos y
lotes de mercadería.

| Entidad | Hoy | Propuesta |
|---|---|---|
| Depósitos y centros | `Ubicacion` en `RedVial` | Igual. Opcional: distinguir depósito y centro en `Ubicacion`. |
| Conexiones | `Conexion` | Igual. |
| Pedidos | `Pedido` con destino `String` | Al cargar un pedido, el sistema valida que su destino exista en la red. No hace falta cambiar `Pedido`. |
| Vehículos | No existe | Nueva clase `Vehiculo`: id, capacidad en kg y depósito de salida. |
| Lotes | `Lote` independiente | Ver la decisión 3.1. |

### 3.1 Decisión abierta: ¿de dónde salen los lotes?

- **Opción A (lotes independientes):** los lotes son mercadería en stock en el depósito, cada uno
  con su prioridad. Es la lectura más literal de la consigna, pero no vincula la F3 con los
  pedidos.
- **Opción B (un lote por pedido):** cada pedido pendiente se convierte en un lote con su mismo
  peso y prioridad, y la F3 responde "¿qué pedidos cargo en este viaje?". Es lo más útil y une la
  F1 con la F3. `Lote` no cambia: la conversión se hace en la capa de sistema y el id del lote es
  el del pedido.

Propuesta: **B como flujo principal**, manteniendo la carga de lotes sueltos (A) para la demo y
las pruebas de la F3. Hay que confirmarlo con el docente.

## 4. Capa de sistema

Paquete nuevo `logistica.sistema` con una clase fachada, por ejemplo `SistemaLogistica`, que:

- guarda la red, los pedidos, los vehículos y los lotes (con arreglos o las estructuras que ya
  usa el proyecto);
- valida la consistencia entre entidades (por ejemplo, que el destino de un pedido exista en la
  red);
- expone operaciones de alto nivel que sólo llaman a los algoritmos existentes:
  - `pedidosPorFecha()` y `pedidosPorPrioridad()` → `MergeSort` (F1)
  - `buscarPedido(id)` → `MergeSort` por id + `BusquedaBinaria` (F1)
  - `rutaMasRapida(origen, destino)`, `deshabilitarConexion(...)` y `habilitarConexion(...)` →
    `Dijkstra` (F2)
  - `armarCarga(vehiculo)` → `SeleccionCargas` (F3)

Tanto la consola como la interfaz gráfica usan esta fachada. Así no se duplica lógica, y la
interfaz nunca llama a los algoritmos directamente.

## 5. Flujo de uso (de punta a punta)

1. **Datos:** se carga la red de ejemplo, los pedidos (con destinos que existen en la red) y uno
   o más vehículos.
2. **Consultar pedidos (F1):** listarlos por fecha límite o por prioridad, y buscar uno por id.
3. **Armar la carga (F3):** se elige un vehículo y el sistema selecciona qué pedidos o lotes
   llevar. Muestra la selección, el peso, la prioridad y la capacidad restante.
4. **Calcular el recorrido (F2):** para cada centro de destino de la carga, se calcula la ruta
   más rápida desde el depósito del vehículo, con su traza. Se puede deshabilitar una conexión y
   recalcular.

Límite importante: si la carga va a varios centros, el sistema muestra **una ruta por destino**
(origen → destino, como pide la F2). Encontrar el **orden óptimo de visita** de varias paradas es
la funcionalidad B1 de la segunda etapa ("Recorrido con múltiples entregas"), así que no se hace
ahora. Si la eligen en la segunda etapa, se integra en este mismo paso.

## 6. Frontend

Propuesta: **Java Swing**. Viene incluido en el JDK, no agrega dependencias y el proyecto sigue
siendo Java puro.

- Paquete `logistica.ui` y un punto de entrada separado (por ejemplo `MainGUI`). `Main` sigue
  siendo la demo por consola.
- Una ventana con pestañas:
  - **Pedidos (F1):** tabla de pedidos, botones "Ordenar por fecha" y "Ordenar por prioridad", y
    un campo para buscar por id.
  - **Rutas (F2):** elegir origen y destino, ver el recorrido y el tiempo, y
    deshabilitar/habilitar conexiones.
  - **Cargas (F3):** elegir el vehículo, ver la selección, los totales y la tabla de
    reconstrucción.
  - **Viaje:** el flujo de la sección 5 de una sola vez.
- Un panel de **traza** en cada pestaña que muestra las decisiones del algoritmo, igual que en la
  consola. La F2 lo exige ("visualizar las principales decisiones").

Cuidados para respetar la materia:

- **No usar el ordenamiento propio de `JTable`** (`setAutoCreateRowSorter` / `TableRowSorter`),
  porque ordena con métodos de Java. El orden que se ve tiene que salir de `MergeSort`.
- **La búsqueda por id usa `BusquedaBinaria`**, no un filtro de la tabla.
- Para mostrar trazas, los algoritmos hoy escriben con `System.out`. Para no modificarlos, la
  interfaz puede redirigir temporalmente la salida a un texto (`System.setOut` con un
  `ByteArrayOutputStream`) mientras corre el algoritmo con traza.

## 7. Fuera de alcance por ahora

- Guardar los datos en archivos o en una base de datos. Si hace falta, se agrega después la
  lectura de un CSV simple, sin librerías.
- Las funcionalidades de la segunda etapa. El modelo queda preparado para A1 (consultas entre
  todos los centros), A2/B4 (asignación de vehículos), B1 (múltiples entregas) y B2 (lotes por
  vehículo).

## 8. Plan de trabajo sugerido (un commit por paso)

1. `Vehiculo` en `modelo/` y sus pruebas.
2. `SistemaLogistica` con las operaciones de la sección 4 y sus pruebas, sin interfaz.
3. Datos de ejemplo consistentes: pedidos cuyos destinos existen en la red.
4. Flujo de punta a punta por consola, como una nueva demo en `Main`.
5. Interfaz Swing: esqueleto con pestañas y panel de traza.
6. Pestañas Pedidos, Rutas y Cargas, una por commit.
7. Pestaña Viaje.
8. Sección del informe que explique la integración y aclare que el frontend es un agregado.

Los pasos 1 a 4 no dependen de la interfaz y pueden avanzar en paralelo con el 5.

## 9. Preguntas para el equipo o el docente

- ¿"Integradas" en la consigna significa un mismo programa o un flujo encadenado?
- Lotes: ¿opción A, B o ambas? (sección 3.1)
- ¿Conviene cambiar el destino de `Pedido` de `String` a `Ubicacion`, aunque implique tocar la F1
  y sus pruebas?
- ¿Mostramos la interfaz en la defensa o sólo la consola?
