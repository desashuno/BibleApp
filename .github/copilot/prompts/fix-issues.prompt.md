---
mode: agent
description: Orquesta el flujo completo de corrección de issues en 3 pasos y solicita decisión del usuario al final.
---

# Fix Issues — Plan de 3 pasos

## Objetivo

Ejecutar el flujo completo de resolución de issues en **3 pasos obligatorios** usando los prompts existentes del repositorio.

---

## Pasos obligatorios

1. **Paso 1 — Análisis de issues**
   - Ejecuta el análisis con: `.github/copilot/prompts/01-red-letter-issue-analysis.prompt.md`
   - Genera o actualiza el reporte de issues con evidencia.

2. **Paso 2 — Diseño de solución**
   - Ejecuta el diseño técnico con: `.github/copilot/prompts/02-red-letter-solution-design.prompt.md`
   - Define plan de implementación, prioridades y riesgos.

3. **Paso 3 — Ejecución de fixes**
   - Ejecuta la implementación con: `.github/copilot/prompts/03-red-letter-fix-execution.prompt.md`
   - Aplica cambios, valida compilación y tests relevantes.

---

## Reglas

- No omitir ninguno de los 3 pasos.
- Completar los pasos en orden (1 → 2 → 3).
- Citar siempre evidencia real del código (archivos y líneas cuando aplique).
- Mantener trazabilidad entre issue detectada, diseño y fix aplicado.

---

## Cierre obligatorio (pregunta al usuario)

Al terminar los 3 pasos, **debes preguntar explícitamente al usuario**:

> "Se encontraron estas issues. ¿Qué quieres que hagamos con cada una: corregir ahora, posponer, o descartar?"

Incluye una lista breve de issues para que el usuario pueda decidir una por una.
