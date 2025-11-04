# A Microservices-based Car Rental System

Repository for the main project of the Web Applications II (2025) course at Politecnico di Torino.

[![React](https://img.shields.io/badge/React-19.1.2-61DAFB?style=flat&logo=react&logoColor=white)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.8.3-3178C6?style=flat&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Leaflet](https://img.shields.io/badge/Leaflet-1.9.4-199900?style=flat&logo=leaflet&logoColor=white)](https://leafletjs.com/)
[![TailwindCSS](https://img.shields.io/badge/TailwindCSS-4.1.7-06B6D4?style=flat&logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-6DB33F?style=flat&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17.4-4169E1?style=flat&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Kafka](https://img.shields.io/badge/Kafka-3.3.2-231F20?style=flat&logo=apache-kafka&logoColor=white)](https://kafka.apache.org/)
[![Keycloak](https://img.shields.io/badge/Keycloak-26.2.4-2C54A3?style=flat&logo=keycloak&logoColor=white)](https://www.keycloak.org/)

<img src="screenshots/landing.png" alt="screen_landing" style="zoom:50%;" />
<img src="screenshots/models.png" alt="screen_car_models" style="zoom:50%;" />
<img src="screenshots/reservation.png" alt="screen_reservation" style="zoom:50%;" />
<img src="screenshots/vehicle.png" alt="screen_vehicle" style="zoom:50%;" />
<img src="screenshots/tracking.png" alt="screen_tracking" style="zoom:50%;" />
<img src="screenshots/analytics.png" alt="screen_analytics" style="zoom:50%;" />

## Getting started

You can run the application with the following docker command:

```bash
docker compose up
```

If you run into problems, append a `--build` flag at the end.

To remove all data and get a fresh start, execute:

```bash
docker compose down -v
```

After running the microservices, you can run the frontend with the following commands:

```bash
cd ReservationFrontend
npm install --force
npm run previewDev
```

Finally, it will be possible to access the website at the address `http://localhost:8083/ui`

## Paypal customer credentials

username: g15-personal@wa2.polito.it
password: bellapass

## Keycloak

### Export real settings

```bash
    docker exec g15-keycloak /opt/keycloak/bin/kc.sh export \
  --file /opt/keycloak/data/export/realm-export.json \
  --realm car-rental-system
```

### Account credentials

Users with role CUSTOMER

```text
username: customer1
password: password

username: customer2
password: password
```

### User with role STAFF

```text
username: staff
password: password
```

### User with role FLEET_MANAGER

```text
username: fleetmanager
password: password
```

### User with role MANAGER

```text
username: manager
password: password
```

## Map Data Preparation

Before you can run `osrm-backend`, you need to prepare the .osrm files from the .osm.pbf.
Run these three commands once inside `osrm` folder (they create files in osrm/data):

```bash
# Extract map data
docker run -t -v "${PWD}/data:/data" osrm/osrm-backend:v5.25.0 osrm-extract -p /opt/car.lua /data/turin.osm.pbf

# Partition map
docker run -t -v "${PWD}/data:/data" osrm/osrm-backend:v5.25.0 osrm-partition /data/turin.osrm

# Customize map
docker run -t -v "${PWD}/data:/data" osrm/osrm-backend:v5.25.0 osrm-customize /data/turin.osrm
```

[OSRM API](https://project-osrm.org/docs/v5.24.0/api)

