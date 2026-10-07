# Instakill — overview și ghid de funcționare

## 1. Ce este proiectul

Instakill este o aplicație socială de tip Instagram, implementată ca un monolith Spring Boot 3 și Vue 3. Scopul principal este să oferă un feed cu postări, like-uri, comentarii și autentificare, cu o bază de date SQLite locală.

Proiectul este structurat pentru a deveni mai târziu o aplicație modulară, dar acum este o aplicație monolitică:

- **Backendul** expune REST și MVC endpoints.
- **Frontendul** este o SPA Vue 3.
- **Baza de date** este SQLite.
- **Autentificarea** utilizează JWT în un cookie HTTP-only.
- **Testele** verifică fluxurile reali și logica de feed.

## 2. Funcționalități

### Autentificare

- Înregistrare cu username, email și parolă.
- Login cu username sau email și parolă.
- Token JWT stocat într-un cookie `ACCESS_TOKEN` HTTP-only.
- Logout elimină cookie-ul și autentificarea frontendă.
- Conturile dezvoltatorului sunt închidate în profilul `dev`.

### Feed

- Listă de postări sortată după:
  - `new` — cea mai recentă prima.
  - `trending` — postările cu cel mai multe like-uri, comentarii și, în caz egal, recency.
- Fiecare post are autor, conținut, imagine optională, date, like-uri și comentarii.
- Paginiunea este pregătită în serviciul de feed, prin parametrii `page` și `size`.

### Postări

- Crearea unui post cu conținut și imagine URL optională.
- Verificarea autorizației prin JWT.
- Gestionarea postărilor prin POST, GET și endpointuri de interacții.

### Interacții

- Like sau unlike pentru o postare.
- Adaugarea și listarea comentariilor.
- Contarile like-uri și commentarii sunt actualizate în răspunsurile API.

### UI

Frontendul oferă:

- Ecran de login și înregistrare.
- Composerul de postări.
- Feedul nou și trending.
- Like-uri și comentarii.
- Logout.

## 3. Arhitectură

### Straturile backendului

```text
api/rest       -> endpointurile REST și DTO-urile de răspuns
api/web        -> endpointurile MVC și formularele Thymeleaf
application   -> servicile de caz și regulile de afaceri
 domain        -> entitățile și interfacenele de domeniu
infrastructure -> JPA, SQLite, JWT, security și configurare
```

Exemplu de flux:

```text
Frontend Vue
    ↓ Axios
REST controller
    ↓
Application service
    ↓
Domain repository
    ↓
JPA adapter
    ↓
SQLite/Hibernate
```

### Structura de directoare

- `backend/src/main/java/com/instakill/api` — interfețe HTTP.
- `backend/src/main/java/com/instakill/feed` — logica de feed.
- `backend/src/main/java/com/instakill/post` — postări.
- `backend/src/main/java/com/instakill/user` — utilizatori și autentificare.
- `backend/src/main/java/com/instakill/interaction` — like-uri și comentarii.
- `backend/src/main/java/com/instakill/infrastructure` — tehnologii concrete.
- `backend/src/main/resources` — configurare și templates.
- `backend/src/test/java` — teste unitare și de integrație.
- `frontend/src` — componenta Vue și clientul Axios.

## 4. Configurarea și dependințe

### Backend

- Java 23 — configurat prin Gradle toolchain.
- Spring Boot 3.3.2.
- Spring Security.
- Spring Data JPA.
- Hibernate SQLite dialect.
- SQLite JDBC.
- JWT JJWT 0.12.6.
- Springdoc OpenAPI.
- Spring Boot Actuator.
- Gradle 8.9.

Fișierul de baza este:

```text
backend/instakill.db
```

Fișierul este ignorat de Git și este creat dacă nu există.

Configurarea principală se află în:

```text
backend/src/main/resources/application.yaml
```

Setările importante:

- `server.servlet.context-path: /`
- `spring.jpa.hibernate.ddl-auto: update`
- `jdbc:sqlite:./instakill.db`
- Swagger la `/api/swagger`
- Health la `/actuator/health`
- JWT secret înscris în configurare

> Secretul JWT este unul de tipul exemplare. În producție trebuie înlocuit cu o valoare secretă, sigură și gestionată prin variabile de mediu.

### Frontend

- Vue 3.4.
- Vite 5.4.
- Axios 1.7.
- `@vitejs/plugin-vue`.

Configurația Vite:

- portul dezvoltatorului: `5173`
- proxy pentru `/api` și `/auth`: `http://localhost:8080`

## 5. Cum funcționează aplicația

### 5.1. Startul backendului

1. Instalează Java 23.
2. Generează wrapper-ul Gradle dacă nu există.
3. Rulează backendul cu:

```powershell
cd backend
.\gradlew.bat bootRun
```

Backendul va fi disponibil la:

```text
http://localhost:8080
```

### 5.2. Startul frontendului

1. Instalează Node.js LTS și npm.
2. Instalează dependințele:

```powershell
cd frontend
npm install
```

3. Startă serverul Vite:

```powershell
npm run dev
```

Frontendul va fi disponibil la:

```text
http://localhost:5173
```

### 5.3. Fluxul de autentificare

1. Browserul deschide frontendul de la `http://localhost:5173`.
2. Utilizatorul se autentifică sau se înregistrează.
3. Axios trimite o cerere POST către `/api/auth/login` sau `/api/auth/register`.
4. Backendul generează JWT și returnează cookie-ul `ACCESS_TOKEN`.
5. Vite proxyază cererea către backendul de la `8080`.
6. Frontendul setează `user` și încărcă feedul.
7. Pentru fiecare cerere ulterioară, backendul verifică cookie-ul JWT.

### 5.4. Fluxul de postare

1. Utilizatorul deschide `Post` în frontend.
2. Frontendul trimită `POST /api/posts`.
3. Backendul verifică autentificarea și creează o postare.
4. Postarea este persistată în SQLite.
5. Frontendul reîncărcă feedul.

## 6. Pașii de dezvoltare locală

### Windows PowerShell

```powershell
cd "C:\Users\andre\OneDrive\Desktop\Instakill\instakill-987b9ea29e93ef4d545c4d7964a266e82c103f99"

# Java 23 și Node.js LTS trebuie să fie instalate
.\scripts\bootstrap-gradle.ps1

# Terminalul 1
cd backend
.\gradlew.bat bootRun

# Terminalul 2
cd frontend
npm install
npm run dev
```

### Conturi de dezvoltare

Accesează:

```text
http://localhost:8080/auth/login
http://localhost:8080/feed
http://localhost:8080/api/swagger
http://localhost:8080/actuator/health
```

Conturile seedate în profilul `dev`:

| Username | Email | Parolă |
|---|---|---|
| alice | alice@example.com | password |
| bob | bob@example.com | password |

## 7. Testarea

Testele sunt în:

```text
backend/src/test/java
```

Ruleazăază testele:

```powershell
cd backend
.\gradlew.bat test
```

Testele acoperă:

- Clasificarea feedului trending.
- Toggle-ul like-uri.
- Fluxurile MVC.
- Fluxul complet REST: register, postare, comment, like și feed.

## 8. Cum funcționează testarea

Testele utilizează baza SQLite în memorie și un profil `test`:

```text
backend/src/test/resources/application-test.yaml
```

Acest profil evită să se apuce baza reală și oferă o bază de test rapidă și izolată.

## 9. Cum funcționează Swagger și health

- OpenAPI documentul: `/api/v3/api-docs`
- Swagger UI: `/api/swagger`
- Health: `/actuator/health`
- Info: `/actuator/info`

## 10. Câtteva de dezvoltare și viitoare funcționalități

Proiectul este deja structurat în module precum user, post, feed și interaction, dar încă este monolitic. Viitoarele etape sunt:

1. Extractarea serviciilor de user, post, feed și interaction.
2. Introducerea API gateway-ului.
3. Introducerea brokerului de mesaje pentru evenimente.
4. Migrația către PostgreSQL pentru producție.
5. Introducerea testelor frontendului cu Vitest și Playwright.
6. Introducerea autentificării OAuth 2.0/OIDC.
7. Îmbunătățirea securității și a configurării de producție.

## 11. Probleme cunoscute în environmentul actual

- Java 19 este instalat, dar proiectul necesită Java 23.
- Node.js și npm nu sunt instalate în environmentul actual.
- Git nu este disponibil în terminalul curent, deci uploadarea pe GitHub va trebui să se facă din VS Code Source Control sau din GitHub CLI, dacă este instalat.
- Gradle wrapper-ul și fișierul de build sunt ignorate și nu sunt în repository.

## 12. Pașii pentru a face repository-ul și a-l publica pe GitHub

1. Instalează Git și autentifică-te cu GitHub.
2. Deschide dacă repo-ul va fi public sau privat.
3. În VS Code, deschide `Source Control` și aleg `Initialize Repository`.
4. Adaugă toate fișierile proiectului:

```powershell
git add .
```

5. Verifică conținutul:

```powershell
git status
```

6. Creați un commit:

```powershell
git commit -m "Initial commit: Instakill social feed"
```

7. Adaugă remote-ul GitHub:

```powershell
git remote add origin https://github.com/USERNAME/REPOSITORY.git
```

8. Trimite în GitHub:

```powershell
git push -u origin main
```

9. Deschide dacă vrei să adaugă un `README.md` și documentația după ce repo-ul va fi publicat.

## 13. Checklist de pregătire

- [ ] Java 23 instalat și verificat.
- [ ] Node.js LTS și npm instalate și verificate.
- [ ] Gradle wrapper generat.
- [ ] `npm install` finalizat.
- [ ] Testele backendului trec.
- [ ] Backendul răspunde la `http://localhost:8080/actuator/health`.
- [ ] Frontendul deschide și afișează feedul.
- [ ] Git sunt autenticat și remote-ul este configurat.
- [ ] Fișierul `instakill.db` nu este adaugat la Git.
- [ ] Secretul JWT nu este expus.
