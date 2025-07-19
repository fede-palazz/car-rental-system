[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/zKPsPXQP)

# Lab 5 - Group 15

## Getting started

You can run the application with the following docker command:

```bash
$ docker compose -f compose-prod.yaml up
```

If you run into problems, append a `--build` flag at the end.

To remove all data and get a fresh start, execute:

```bash
docker compose -f compose-prod.yaml down -v
```

## Paypal customer credentials

username: g15-personal@wa2.polito.it
password: bellapass

## Keycloak credentials

### Users with role CUSTOMER

username: customer1
password: password

username: customer2
password: password

### User with role STAFF

username: staff
password: password

### User with role FLEET_MANAGER

username: fleetmanager
password: password

### User with role MANAGER

username: manager
password: password
