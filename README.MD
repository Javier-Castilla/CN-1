<div align="center">
  <img src="https://www.eii.ulpgc.es/sites/default/files/eii-acron-mod.png"
       alt="Logo ULPGC"
       width="500"
       style="margin-bottom: 10px;">
</div>

<h1 align="center">Práctica Obligatoria 1</h1>

<div align="center" style="font-family: 'Segoe UI', sans-serif; line-height: 1.6; margin-top: 30px;">
  <h2 style="font-size: 28px; margin-bottom: 10px;">
    Asignatura: <span>Computación en la Nube</span>
  </h2>
  <p style="font-size: 18px; margin: 4px 0;">
    Grado en Ingeniería Informática
  </p>
  <p style="font-size: 18px; margin-top: 10px;">
    Curso <strong>2025 / 2026</strong>
  </p>
</div>

<h2 align="center">Estructura</h2>
El repositorio contiene el código de la API implementada en Java SpringBoot correspondiente a dos tipos de arquitecturas, acoplada y desacoplada. Para ello, se han separado los diferentes servicios en 3 módulos, siendo el proyecto principal el encargado de orquestrarlos en la arquitectura acoplada (monolito). En el caso de la arquitectura desacoplada (microservicios), cada módulo se ejecuta dentro de su Docker correspondiente.

- root
  - books
    - src
    - ...
  - customers
    - src
    - ...
  - orders
    - src
    - ...
  - monolith
    - src
    - ...

Estos son los diagramas restantes:

<h2 align="center">Estructura Acoplada</h2>

<img src="CN-ECS.jpg">

<h2 align="center">Estructura Descoplada</h2>

<img src="CN-Lambdas.jpg">
