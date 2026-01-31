# Prompt de Integración: Capacitor Plugin en Proyecto Existente (Migración a Capacitor)

**Contexto:**
Estoy trabajando con dos proyectos locales:
1. **Plugin (Origen):** `capacitor-appodeal` (Ubicado en: `/home/ripper2hl/Documentos/git/capacitor-appodeal`). Este es un plugin de Capacitor v6 verificado que acabo de crear.
2. **App Principal (Destino):** `sepomex-ui` (Ubicado en: `/home/ripper2hl/Documentos/git/sepomex-ui`). Esta es una aplicación Ionic/Angular que actualmente usa **Cordova**.

**Objetivo:**
Quiero integrar mi plugin local `capacitor-appodeal` en la aplicación `sepomex-ui`. Dado que la app aún no tiene Capacitor configurado, necesito migrar o adaptar la app para usar Capacitor y así poder consumir este nuevo plugin.

**Instrucciones para el Agente:**
Por favor, realiza las siguientes tareas paso a paso:

1.  **Inicializar Capacitor en `sepomex-ui`:**
    *   Instala `@capacitor/core`, `@capacitor/cli`, `@capacitor/android` y `@capacitor/ios`.
    *   Inicializa Capacitor con `npx cap init` (manten el mismo App ID y Nombre que en config.xml si es posible).
    *   Asegúrate de que la configuración de construcción (`dist` folder) coincida en `capacitor.config.ts`.

2.  **Instalar el Plugin Local:**
    *   Instala el plugin desde la ruta local: `npm install /home/ripper2hl/Documentos/git/capacitor-appodeal`.
    *   Verifica que se haya añadido a `package.json`.

3.  **Configurar Android:**
    *   Añade la plataforma Android: `npx cap add android`.
    *   Configura los repositorios necesarios en `android/build.gradle` de la App (como aprendimos en el plugin, necesitamos el repo de Appodeal):
        ```groovy
        maven { url "https://artifactory.appodeal.com/appodeal" }
        ```
    *   Sincroniza el proyecto: `npx cap sync`.

4.  **Verificación:**
    *   Intenta compilar la App Android para asegurar que Capacitor, los plugins de Cordova existentes (que Capacitor debería soportar vía compatibilidad) y el nuevo plugin de Appodeal conviven correctamente.

**Nota Técnica:**
El proyecto `sepomex-ui` usa Angular 16. Asegúrate de instalar versiones de Capacitor compatibles (Capacitor 5 o 6 deberían funcionar bien, preferiblemente 6).
