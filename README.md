# 🗺️ Metamapa Client
![Java](https://img.shields.io/badge/Java-17+-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-View%20Engine-blue)

Repositorio para probar, con mocks, el funcionamiento de una aplicación web **MVC**, desarrollada en el marco de un proyecto académico de la UTN.

Este módulo corresponde al servicio **clienteInterfaz** del proyecto **Metamapa**, y está desarrollado con **Spring Boot + Thymeleaf**.

---

## 🚀 Cómo levantar el proyecto

### 🧩 Requisitos

| Sistema | Requisitos |
|----------|-------------|
| **macOS / Linux** | Tener instalados:<br>• [Java 17+](https://adoptium.net/)<br>• [Maven](https://maven.apache.org/) (`brew install openjdk@17 maven` en macOS)<br>• Acceso a internet para dependencias de Maven |
| **Windows** | Instalar:<br>• [Java 17+](https://adoptium.net/)<br>• [Maven](https://maven.apache.org/download.cgi)<br>• Agregar `JAVA_HOME` y `MAVEN_HOME` al PATH si fuera necesario |

Verificá la instalación con:
```bash
java -version
mvn -version
```

---

### ⚙️ Configuración

El archivo `application.properties` **no se encuentra versionado** (por seguridad), pero debe incluir configuraciones como las siguientes:

```properties
spring.application.name=metamapa-client
server.port=8080
spring.thymeleaf.cache=false
spring.profiles.active=dev

# ---- URLs de servicios ----
auth.service.url=http://localhost:6001
api.servicioUsuarios.url=http://localhost:6001
agregador.api.base-url=https://tu-api-o-servidor.com

# ---- OAuth2 (Google / GitHub) ----
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.github.client-id=${GITHUB_CLIENT_ID}
spring.security.oauth2.client.registration.github.client-secret=${GITHUB_CLIENT_SECRET}
```

> 💡 Las credenciales (`GOOGLE_CLIENT_ID`, etc.) deben definirse como variables de entorno o en un archivo `.env` local (no versionado).

#### 🔧 Definir variables de entorno

**macOS / Linux**

```bash
export GOOGLE_CLIENT_ID=tu_client_id
export GOOGLE_CLIENT_SECRET=tu_client_secret
export GITHUB_CLIENT_ID=tu_client_id
export GITHUB_CLIENT_SECRET=tu_client_secret
```

**Windows (PowerShell)**

```powershell
setx GOOGLE_CLIENT_ID "tu_client_id"
setx GOOGLE_CLIENT_SECRET "tu_client_secret"
setx GITHUB_CLIENT_ID "tu_client_id"
setx GITHUB_CLIENT_SECRET "tu_client_secret"
```

---

 ### 🔑 Cómo generar credenciales OAuth2
 
<details><summary>Para Google</summary>
   
#### 🟦 Google
1. Ingresá a [Google Cloud Console](https://console.cloud.google.com/).
2. Creá un nuevo proyecto o usá uno existente.
3. Activá la API **OAuth consent screen** (pantalla de consentimiento).
4. En la sección **Credentials → Create credentials → OAuth client ID**, elegí:
   - Application type: **Web application**
   - Authorized redirect URI:  
     ```
     http://localhost:8080/login/oauth2/code/google
     ```
5. Guardá los valores generados (`Client ID` y `Client Secret`) y definilos como variables de entorno según tu sistema operativo:

##### 💻 macOS / Linux
```bash
export GOOGLE_CLIENT_ID=tu_client_id
export GOOGLE_CLIENT_SECRET=tu_client_secret
```

##### 🪟 Windows (PowerShell)

```powershell
setx GOOGLE_CLIENT_ID "tu_client_id"
setx GOOGLE_CLIENT_SECRET "tu_client_secret"
```

</details>

<details><summary>Para Github</summary>
   
#### 🐙 GitHub

1. Ingresá a [GitHub Developer Settings → OAuth Apps](https://github.com/settings/developers).
2. Clic en **New OAuth App**.
3. Completá los campos:

   * **Homepage URL:** `http://localhost:8080`
   * **Authorization callback URL:**

     ```
     http://localhost:8080/login/oauth2/code/github
     ```
4. Una vez creado, copiá el `Client ID` y generá un nuevo `Client Secret`.
5. Definilos como variables de entorno según tu sistema operativo:

##### 💻 macOS / Linux

```bash
export GITHUB_CLIENT_ID=tu_client_id
export GITHUB_CLIENT_SECRET=tu_client_secret
```

##### 🪟 Windows (PowerShell)

```powershell
setx GITHUB_CLIENT_ID "tu_client_id"
setx GITHUB_CLIENT_SECRET "tu_client_secret"
```

</details>


> ⚠️ **Importante:** nunca subas tus credenciales reales a GitHub.
> Guardalas solo en tu entorno local (por ejemplo, en un archivo `.env` o en tu configuración de sistema) y asegurate de que estén incluidas en el `.gitignore`.

---

### ▶️ Ejecución

Desde la raíz del proyecto:

```bash
mvn spring-boot:run
```

Luego abrí en tu navegador:

👉 [http://localhost:8080](http://localhost:8080)

---

### 🧠 Notas

* Si los servicios backend (`servicioUsuarios`, `servicioAgregador`) no están levantados, podés usar **mocks** o servidores locales para pruebas.
* Los logs HTTP se pueden ver gracias a:

  ```properties
  logging.level.org.springframework.web.reactive.function.client=DEBUG
  ```
* El proyecto utiliza **Thymeleaf** como motor de plantillas y **Spring Security** para autenticación OAuth2.

---

### 📚 Tecnologías principales

* Java 17+
* Spring Boot
* Thymeleaf
* OAuth2 (Google / GitHub)
* Maven

---

### 🎓 Proyecto académico

Proyecto académico de la **Facultad Regional Buenos Aires - UTN**.
Este repositorio fue creado para pruebas del servicio **clienteInterfaz** del proyecto **Metamapa**.

