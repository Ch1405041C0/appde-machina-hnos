# Machina Hnos — MVP App Vehículos

## Objetivo

La app no funciona como un catálogo tradicional. Su función principal es recomendar, dentro del stock real disponible de Machina Hnos, los vehículos que mejor encajan con el uso y las preferencias del cliente.

## Flujo principal

1. Inicio
2. Encontrá tu auto
3. Cuestionario dinámico
4. Perfil del comprador
5. Ranking de vehículos disponibles
6. Resultado con porcentaje de compatibilidad
7. Ficha del vehículo
8. Contacto con asesor

También existe un acceso alternativo al catálogo completo para el usuario que prefiera explorar manualmente.

## Principios de producto

- No preguntar presupuesto en el flujo principal.
- Recomendar únicamente vehículos realmente disponibles.
- Clasificar automáticamente cada vehículo cargado.
- Las preguntas pueden variar según el stock disponible.
- La primera versión no necesita IA generativa: el valor está en la clasificación, el scoring y el matching.
- La ficha debe explicar por qué un vehículo encaja con el usuario.

## Perfil de vehículo

Cada unidad debe contar, como mínimo, con:

- id
- marca
- modelo / versión
- año
- kilometraje
- precio
- carrocería
- combustible
- transmisión
- tracción
- cantidad de plazas
- usos compatibles
- prioridades / atributos
- estado de publicación
- imágenes

Además, el sistema deberá poder derivar atributos como:

- segmento
- personalidad / posicionamiento
- uso recomendado
- condición relativa del kilometraje según antigüedad

Ejemplos de personalidad: urbano, familiar, aventurero, ejecutivo, trabajador, deportivo, clásico.

## Preguntas iniciales

El cuestionario debe cubrir:

- uso principal: trabajo, personal, familiar o mixto
- tipo de vehículo preferido
- entorno de uso: ciudad, ruta, campo, carga o combinación
- prioridad: economía, espacio, robustez, confort, seguridad, tecnología, prestaciones, presencia
- combustible
- transmisión
- necesidad de 4x4
- preferencia por bajo kilometraje

No todas las preguntas tienen que aparecer siempre: el cuestionario debe adaptarse al stock.

## Arquitectura MVP

### Cliente

Android nativo con Kotlin + Jetpack Compose.

### Dominio

- `VehicleClassifier`: clasifica unidades.
- `RecommendationEngine`: puntúa compatibilidad.
- `DynamicQuestionEngine` / `QuestionnaireFlow`: define preguntas según catálogo.
- `RecommendationSession`: mantiene el contexto de recomendación.

### Datos

Estado actual: catálogo demo embebido en la app.

Próximo paso: reemplazarlo por un `VehicleRepository` para desacoplar UI y motor de recomendaciones del origen de datos. En desarrollo puede usar datos locales y luego conectarse a Supabase/API sin reescribir las pantallas.

## Definición de terminado para la V1

La V1 queda lista para validación cuando:

- la app inicia correctamente
- el cuestionario puede completarse de punta a punta
- genera un ranking reproducible
- cada recomendación explica sus motivos
- catálogo y detalle funcionan
- los vehículos provienen de un repositorio desacoplado
- existe una acción clara para contactar a un asesor
- el stock puede reemplazarse sin modificar el motor de recomendación

## Próxima iteración

1. Crear `VehicleRepository`.
2. Mover los vehículos demo fuera de `MainActivity`.
3. Agregar estado `available` / `reserved` / `sold`.
4. Incorporar fotos reales.
5. Agregar CTA de WhatsApp/asesor.
6. Conectar una fuente remota de stock.
7. Crear panel web de administración para altas, bajas y cambios de unidades.
