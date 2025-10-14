package com.rentalcarsystem.reservationservice.services

import com.rentalcarsystem.reservationservice.dtos.request.CarModelReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.toEntity
import com.rentalcarsystem.reservationservice.dtos.response.*
import com.rentalcarsystem.reservationservice.enums.*
import com.rentalcarsystem.reservationservice.exceptions.FailureException
import com.rentalcarsystem.reservationservice.exceptions.ResponseEnum
import com.rentalcarsystem.reservationservice.filters.CarModelFilter
import com.rentalcarsystem.reservationservice.models.CarFeature
import com.rentalcarsystem.reservationservice.models.CarModel
import com.rentalcarsystem.reservationservice.models.Maintenance
import com.rentalcarsystem.reservationservice.models.Reservation
import com.rentalcarsystem.reservationservice.models.Vehicle
import com.rentalcarsystem.reservationservice.repositories.CarFeatureRepository
import com.rentalcarsystem.reservationservice.repositories.CarModelRepository
import com.rentalcarsystem.reservationservice.repositories.ReservationRepository
import jakarta.persistence.criteria.Predicate
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.validation.annotation.Validated
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.time.LocalDateTime

@Service
@Transactional
@Validated
class CarModelServiceImpl(
    private val carModelRepository: CarModelRepository,
    private val carFeatureRepository: CarFeatureRepository,
    private val reservationRepository: ReservationRepository,
    private val vehicleService: VehicleService,
    private val userManagementRestClient: RestClient,
    private val keycloakTokenRestClient: RestClient,
    @Value("\${reservation.buffer-days}")
    private val reservationBufferDays: Long,
    @Value("\${spring.security.oauth2.client.registration.keycloak.client-id}")
    private val clientId: String,
    @Value("\${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private val clientSecret: String
) : CarModelService {
    fun getAccessToken(): String {
        val body = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "client_credentials")
            add("client_id", clientId)
            add("client_secret", clientSecret)
        }
        //println(body)
        val response: Map<*, *> = keycloakTokenRestClient
            .post()
            .uri("") // Base URL already includes token path
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(body)
            .retrieve()
            .body(Map::class.java)!!

        return response["access_token"] as String
    }

    override fun getAllModels(
        page: Int,
        size: Int,
        singlePage: Boolean,
        sortBy: String,
        sortOrder: String,
        @Valid filters: CarModelFilter,
        desiredPickUpDate: LocalDateTime?,
        desiredDropOffDate: LocalDateTime?,
        customerUsername: String?,
        isCustomer: Boolean,
        reservationToUpdateId: Long?
    ): PagedResDTO<CarModelResDTO> {
        // Validation of reservationToUpdate if provided
        if (reservationToUpdateId != null) {
            val reservationToUpdate = reservationRepository.findById(reservationToUpdateId).orElseThrow {
                FailureException(
                    ResponseEnum.RESERVATION_NOT_FOUND,
                    "Reservation with id $reservationToUpdateId was not found"
                )
            }

            if (isCustomer && reservationToUpdate.customerUsername != customerUsername) {
                throw IllegalArgumentException(
                    "You are not allowed to search for available models with customer id $customerUsername and reservation $reservationToUpdateId"
                )
            }
        }

        var spec: Specification<CarModel> = Specification.where(null)
        // Brand
        filters.brand?.takeIf { it.isNotBlank() }?.let { brand ->
            spec = spec.and { root, _, cb ->
                cb.like(cb.lower(root.get("brand")), "${brand.lowercase()}%")
            }
        }
        // Model
        filters.model?.takeIf { it.isNotBlank() }?.let { model ->
            spec = spec.and { root, _, cb ->
                cb.like(cb.lower(root.get("model")), "${model.lowercase()}%")
            }
        }
        // Year
        filters.year?.let { year ->
            spec = spec.and { root, _, cb ->
                cb.equal(root.get<String>("year"), year.toString())
            }
        }
        // Full text search on brand, model and year
        filters.search?.takeIf { it.isNotBlank() }?.let { search ->
            spec = spec.and(buildSearchSpecFromFreeText(search))
        }
        // Segment
        filters.segment?.let { segment ->
            spec = spec.and { root, _, cb ->
                cb.equal(root.get<CarSegment>("segment"), segment)
            }
        }
        // Category
        filters.category?.let { category ->
            spec = spec.and { root, _, cb ->
                cb.equal(root.get<CarCategory>("category"), category)
            }
        }
        // Engine type
        filters.engineType?.let { engineType ->
            spec = spec.and { root, _, cb ->
                cb.equal(root.get<EngineType>("engineType"), engineType)
            }
        }
        // Transmission type
        filters.transmissionType?.let { transmissionType ->
            spec = spec.and { root, _, cb ->
                cb.equal(root.get<TransmissionType>("transmissionType"), transmissionType)
            }
        }
        // Drivetrain
        filters.drivetrain?.let { drivetrain ->
            spec = spec.and { root, _, cb ->
                cb.equal(root.get<Drivetrain>("drivetrain"), drivetrain)
            }
        }
        // Rental price
        filters.minRentalPrice?.let { minPrice ->
            spec = spec.and { root, _, cb ->
                cb.greaterThanOrEqualTo(root.get("rentalPrice"), minPrice)
            }
        }
        filters.maxRentalPrice?.let { maxPrice ->
            spec = spec.and { root, _, cb ->
                cb.lessThanOrEqualTo(root.get("rentalPrice"), maxPrice)
            }
        }
        // Adding the search for desired pickup and drop off dates for a given customer if they are given
        if (desiredPickUpDate != null && desiredDropOffDate != null) {
            spec =
                spec.and(availabilityInDesiredDatesSpec(desiredPickUpDate, desiredDropOffDate, reservationToUpdateId))
            if (isCustomer && customerUsername != null) {
                // TODO: Replace "uri("/{userId}", customerId)" with
                //  "uri("/{userId}", if (user is logged in with role == customer) { logged in user's id } else customerId)"
                val token = getAccessToken()
                val userScore = userManagementRestClient.get().uri("/username/{username}", customerUsername)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token").accept(
                    APPLICATION_JSON
                ).retrieve().body<UserResDTO>()?.eligibilityScore
                spec = spec.and(availabilityForUserScoreSpec(userScore!!))
            }
        }
        // Sorting
        val sortOrd: Sort.Direction = if (sortOrder == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val actualSize = if (singlePage) carModelRepository.count().toInt() else size
        val pageable: Pageable = PageRequest.of(page, actualSize, sortOrd, sortBy)
        val pageResult = carModelRepository.findAll(spec, pageable)

        return PagedResDTO(
            currentPage = pageResult.number,
            totalPages = pageResult.totalPages,
            totalElements = pageResult.totalElements,
            elementsInPage = pageResult.numberOfElements,
            content = pageResult.content.map { it.toResDTO() }
        )
    }

    override fun getCarModelById(id: Long): CarModelResDTO {
        return getActualCarModelById(id).toResDTO()
    }

    override fun getAllCarFeatures(): List<CarFeatureResDTO> {
        return carFeatureRepository.findAll().map { it.toResDTO() }
    }

    override fun getCarFeatureById(id: Long): CarFeatureResDTO {
        return carFeatureRepository.findById(id).map { it.toResDTO() }
            .orElseThrow {
                FailureException(ResponseEnum.CAR_MODEL_NOT_FOUND, "Car model with ID $id not found")
            }
    }

    override fun getCarFeaturesByCarModelId(carModelId: Long): List<CarFeatureResDTO> {
        return getActualCarModelById(carModelId).features.map { it.toResDTO() }
    }

    override fun createCarModel(@Valid model: CarModelReqDTO): CarModelResDTO {
        try {
            // Retrieve car features info
            val features: List<CarFeature> = carFeatureRepository.findAllById(model.featureIds)
            if (features.size != model.featureIds.size) {
                throw NoSuchElementException("One or more features ids are invalid")
            }
            // Create model to persist and reference the list of features
            val newModel = model.toEntity(features.toMutableSet())
            return carModelRepository.save(newModel).toResDTO()
        } catch (e: Exception) {
            throw FailureException(ResponseEnum.CAR_MODEL_DUPLICATED)
        }
    }

    override fun updateCarModel(id: Long, @Valid model: CarModelReqDTO): CarModelResDTO {
        // Check if car model exists
        val modelToUpdate = getActualCarModelById(id)
        // Retrieve car features info
        val features: List<CarFeature> = carFeatureRepository.findAllById(model.featureIds)
        // Check that all the ids correspond to a valid feature
        if (features.size != model.featureIds.size) {
            throw NoSuchElementException("One or more features ids are invalid")
        }
        // Update the fields
        modelToUpdate.brand = model.brand
        modelToUpdate.model = model.model
        modelToUpdate.year = model.year
        modelToUpdate.segment = model.segment
        modelToUpdate.doorsNumber = model.doorsNumber
        modelToUpdate.seatingCapacity = model.seatingCapacity
        modelToUpdate.luggageCapacity = model.luggageCapacity
        modelToUpdate.category = model.category
        modelToUpdate.features = features.toMutableSet()
        modelToUpdate.engineType = model.engineType
        modelToUpdate.transmissionType = model.transmissionType
        modelToUpdate.drivetrain = model.drivetrain
        modelToUpdate.motorDisplacement = model.motorDisplacement
        modelToUpdate.rentalPrice = model.rentalPrice
        return modelToUpdate.toResDTO()
    }

    override fun deleteCarModelById(id: Long) {
        // Check if car model exists
        val modelToDelete = getActualCarModelById(id)
        // Delete all the corresponding vehicles
        vehicleService.deleteAllByCarModelId(id)
        // Delete the car model
        carModelRepository.delete(modelToDelete)
    }

    override fun getActualCarModelById(id: Long): CarModel {
        return carModelRepository.findById(id)
            .orElseThrow {
                FailureException(ResponseEnum.CAR_MODEL_NOT_FOUND, "Car model with ID $id not found")
            }
    }

    /************************************************/

    fun availabilityInDesiredDatesSpec(
        desiredStart: LocalDateTime,
        desiredEnd: LocalDateTime,
        reservationToUpdateId: Long?
    ): Specification<CarModel> {
        return Specification { root, query, cb ->
            // Ensures that the result list has unique CarModels
            query!!.distinct(true)
            // Creates a subquery that returns a Long (the CarModel.id)
            val subquery = query.subquery(Long::class.java)
            // vehicleRoot is the root of this subquery — we’re querying the Vehicle table.
            val vehicleRoot = subquery.from(Vehicle::class.java)
            // Ensures the subquery's Vehicle.carModel matches the outer query’s CarModel (i.e., the one being tested).
            val modelMatch = cb.equal(vehicleRoot.get<CarModel>("carModel"), root)

            // Creates an inner subquery that returns the ids of all vehicles that have at least one overlapping reservation
            val overlappingReservationsVehicles = subquery.subquery(Long::class.java)
            // reservationRoot is the root of this subquery — we’re querying the Reservation table.
            val reservationRoot = overlappingReservationsVehicles.from(Reservation::class.java)
            // Ensures the inner subquery's Reservation.vehicle matches the subquery’s Vehicle (i.e., the one being tested).
            val reservationVehicleMatch = cb.equal(reservationRoot.get<Vehicle>("vehicle"), vehicleRoot)
            // Only considers reservations that overlap the desired date range.
            val overlappingReservation = cb.and(
                cb.lessThan(
                    reservationRoot.get("plannedPickUpDate"),
                    desiredEnd.plusDays(reservationBufferDays)
                ),
                cb.greaterThan(
                    reservationRoot.get("bufferedDropOffDate"),
                    desiredStart
                )
            )
            // Ensures that, if a reservation is given, the vehicle associated to such reservation won't be considered as overlapping with itself
            val reservedVehicleExclusion = reservationToUpdateId?.let { reservationToUpdateId ->
                cb.notEqual(reservationRoot.get<Long>("id"), reservationToUpdateId)
            } ?: cb.notEqual(reservationRoot.get<Long>("id"), 0)
            // Assembles the inner subquery filters: Belongs to current Vehicle AND is reserved during the desired time AND does not match with the given reservation
            overlappingReservationsVehicles.where(
                cb.and(
                    reservationVehicleMatch,
                    overlappingReservation,
                    reservedVehicleExclusion
                )
            )
            // The inner subquery returns the ID of the Vehicle associated with each matching Reservation.
            overlappingReservationsVehicles.select(reservationRoot.get<Vehicle>("vehicle").get("id"))
            // The final condition is: such a reservation exists for the given Vehicle. If this is NOT true, the subquery will include the Vehicle.
            val hasNoOverlappingReservations = cb.not(cb.exists(overlappingReservationsVehicles))

            // Creates an inner subquery that returns the ids of all vehicles that have at least one overlapping maintenance
            val overlappingMaintenancesVehicles = subquery.subquery(Long::class.java)
            // maintenanceRoot is the root of this subquery — we’re querying the Maintenance table.
            val maintenanceRoot = overlappingMaintenancesVehicles.from(Maintenance::class.java)
            // Ensures the inner subquery's Maintenance.vehicle matches the subquery’s Vehicle (i.e., the one being tested).
            val maintenanceVehicleMatch  = cb.equal(maintenanceRoot.get<Vehicle>("vehicle"), vehicleRoot)
            // Only considers maintenances that overlap the desired date range. If actualEndDate is null, it uses plannedEndDate
            val effectiveEndDate = cb.coalesce<LocalDateTime>(
                maintenanceRoot.get("actualEndDate"),
                maintenanceRoot.get("plannedEndDate")
            )
            val overlappingMaintenance = cb.and(
                cb.lessThanOrEqualTo(
                    maintenanceRoot.get("startDate"),
                    desiredEnd
                ),
                cb.greaterThanOrEqualTo(
                    effectiveEndDate,
                    desiredStart
                )
            )
            // Assembles the inner subquery filters: Belongs to current Vehicle AND is in maintenance during the desired time
            overlappingMaintenancesVehicles.where(
                cb.and(
                    maintenanceVehicleMatch,
                    overlappingMaintenance
                )
            )
            // The inner subquery returns the ID of the Vehicle associated with each matching Maintenance.
            overlappingMaintenancesVehicles.select(maintenanceRoot.get<Vehicle>("vehicle").get("id"))
            // The final condition is: such a maintenance exists for the given Vehicle. If this is NOT true, the subquery will include the Vehicle.
            val hasNoOverlappingMaintenances = cb.not(cb.exists(overlappingMaintenancesVehicles))

            // Assembles the subquery filters: Belongs to current CarModel AND is not reserved during the desired time AND is not in maintenance during the desired time
            subquery.where(cb.and(modelMatch, hasNoOverlappingReservations, hasNoOverlappingMaintenances))
            // The subquery returns the ID of the CarModel associated with each matching Vehicle.
            subquery.select(vehicleRoot.get<CarModel>("carModel").get("id"))
            // The final condition is: such a vehicle exists for the given CarModel. If this is true, the outer query will include the CarModel in the result.
            cb.exists(subquery)
        }
    }

    fun availabilityForUserScoreSpec(userScore: Double): Specification<CarModel> {
        return Specification { root, _, cb ->
            // Find all CarCategories where the required threshold is less than or equal to the user's score
            val rentableCategories = CarCategory.entries.filter { CarCategory.getValue(it)!! <= userScore }
            // Get a reference to the 'category' column of the CarModel table
            val categoryPath = root.get<CarCategory>("category")
            // Create an 'IN' clause (WHERE category IN (...))
            val inClause = cb.`in`(categoryPath)
            // For each rentable category, add it to the 'IN' clause
            rentableCategories.forEach { inClause.value(it) }
            // Return the final predicate that will be applied to the query
            inClause
        }
    }

    fun buildSearchSpecFromFreeText(raw: String): Specification<CarModel> {
        val cleaned = raw.trim().replace(Regex("\\s+"), " ")
        val yearRegex = Regex("""\b(19|20)\d{2}\b""")
        val yearMatch = yearRegex.find(cleaned)
        val yearToken = yearMatch?.value

        // remove year token from text
        val textPart = if (yearMatch != null) {
            (cleaned.removeRange(yearMatch.range)).trim().replace(Regex("\\s+"), " ")
        } else cleaned

        return Specification { root, query, cb ->
            val predicates = mutableListOf<Predicate>()

            // Year predicate (year stored as VARCHAR(4) in your schema)
            val yearPredicate = yearToken?.let { cb.equal(root.get<String>("year"), it) }

            // If there's textual part, build FTS + trigram/ILIKE predicates
            val textPredicate: Predicate? = textPart.takeIf { it.isNotBlank() }?.let { text ->
                // 1) FTS: ts_rank_cd(search_vector, plainto_tsquery('simple', :text)) > 0
                // Use the entity property name for the stored tsvector column (e.g. "searchVector").
                val searchVectorExpr = root.get<Any>("searchVector") // map the entity attribute name
                val tsqueryExpr = cb.function(
                    "plainto_tsquery",
                    String::class.java,
                    cb.literal("simple"),
                    cb.literal(text)
                )
                val rankExpr = cb.function(
                    "ts_rank_cd",
                    Double::class.java,
                    searchVectorExpr,
                    tsqueryExpr
                )
                val ftsPredicate = cb.greaterThan(rankExpr, 0.0)

                // 2) Trigram / ILIKE fallbacks - brand, model, combined brand + ' ' + model
                val lowerBrand = cb.lower(root.get("brand"))
                val lowerModel = cb.lower(root.get("model"))
                val lowerTextParam = text.lowercase()

                val brandLike = cb.like(lowerBrand, "$lowerTextParam%")        // prefix on brand
                val modelLike = cb.like(lowerModel, "$lowerTextParam%")        // prefix on model
                val brandModelConcat = cb.lower(
                    cb.concat(
                        cb.concat(root.get<String>("brand"), cb.literal(" ")),
                        root.get<String>("model")
                    )
                )
                val brandModelLike = cb.like(brandModelConcat, "%$lowerTextParam%") // substring on combined

                // Additionally consider substring matches on brand or model:
                val brandSubstring = cb.like(lowerBrand, "%$lowerTextParam%")
                val modelSubstring = cb.like(lowerModel, "%$lowerTextParam%")

                // Combine FTS OR any ILIKE/trigram predicate
                cb.or(ftsPredicate, brandLike, modelLike, brandModelLike, brandSubstring, modelSubstring)
            }

            // Combine searchPredicate and yearPredicate correctly:
            val finalPred = when {
                textPredicate != null && yearPredicate != null -> cb.and(textPredicate, yearPredicate)
                textPredicate != null -> textPredicate
                yearPredicate != null -> yearPredicate
                else -> cb.conjunction()
            }

            finalPred
        }
    }
}