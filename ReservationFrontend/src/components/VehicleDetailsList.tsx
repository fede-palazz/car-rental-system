import { Vehicle } from "@/models/Vehicle";

function VehicleDetailsList({
  vehicle,
  isInCard = false,
  isOutsideForm = true,
}: {
  vehicle: Vehicle | undefined;
  isInCard?: boolean;
  isOutsideForm?: boolean;
}) {
  const cols = isInCard ? "2" : "3";
  return (
    vehicle && (
      <ul
        className={`mt-4 px-6 grid grid-cols-2 lg:grid-cols-${cols} gap-y-4 w-full justify-items-center text-md`}>
        <li className="w-full grid grid-cols-3 gap-x-4 ">
          <div className="flex justify-end items-center col-span-1">
            <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
              brand_family
            </span>
          </div>
          <div className="space-y-1 text-start col-span-2">
            <p>
              <strong className="text-accent-foreground text-lg">Brand</strong>
            </p>
            {vehicle.brand}
          </div>
        </li>
        <li className="w-full grid grid-cols-3 gap-x-4 ">
          <div className="flex justify-end items-center  col-span-1">
            <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
              directions_car{" "}
            </span>
          </div>
          <div className="space-y-1 text-start  col-span-2">
            <p>
              <strong className="text-accent-foreground text-lg">Model</strong>
            </p>
            {vehicle.model}
          </div>
        </li>
        <li className="w-full grid grid-cols-3 gap-x-4 ">
          <div className="flex justify-end items-center col-span-1">
            <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
              car_tag
            </span>
          </div>
          <div className="space-y-1 break-words text-start col-span-2">
            <p>
              <strong className="text-accent-foreground text-lg">Vin</strong>
            </p>
            {vehicle.vin}
          </div>
        </li>
        <li className="w-full grid grid-cols-3 gap-x-4 ">
          <div className="flex justify-end items-center col-span-1 ">
            <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
              calendar_today
            </span>
          </div>
          <div className="space-y-1 text-start col-span-2">
            <p>
              <strong className="text-accent-foreground text-lg">Year</strong>
            </p>
            {vehicle.year}
          </div>
        </li>

        <li className="w-full grid grid-cols-3 gap-x-4 ">
          <div className="flex justify-end items-center col-span-1">
            <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
              tag
            </span>
          </div>
          <div className="space-y-1 text-start col-span-2">
            <p>
              <strong className="text-accent-foreground text-lg">
                License Plate
              </strong>
            </p>
            {vehicle.licensePlate}
          </div>
        </li>
        {isOutsideForm && (
          <li className="w-full grid grid-cols-3 gap-x-4 ">
            <div className="flex justify-end items-center col-span-1">
              <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
                adjust
              </span>
            </div>
            <div className="space-y-1 text-start col-span-2">
              <p>
                <strong className="text-accent-foreground text-lg">
                  Status
                </strong>
              </p>
              {vehicle.status.replace(/_/g, " ").charAt(0).toUpperCase() +
                vehicle.status.replace(/_/g, " ").slice(1).toLowerCase()}
            </div>
          </li>
        )}
        <li className="w-full grid grid-cols-3 gap-x-4 ">
          <div className="flex justify-end items-center col-span-1">
            <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
              distance
            </span>
          </div>
          <div className="text-start space-y-1  col-span-2">
            <p>
              <strong className="text-accent-foreground text-lg">
                Km Travelled
              </strong>
            </p>
            {vehicle.kmTravelled}
          </div>
        </li>
        {isOutsideForm && (
          <>
            <li className="w-full grid grid-cols-3 gap-x-4 ">
              <div className="flex justify-end items-center  col-span-1">
                <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
                  local_car_wash
                </span>
              </div>
              <div className="text-start col-span-2 space-y-1 ">
                <p>
                  <strong className="text-accent-foreground text-lg">
                    Pending Cleaning
                  </strong>
                </p>
                {vehicle.pendingCleaning ? "Yes" : "No"}
              </div>
            </li>
          </>
        )}
      </ul>
    )
  );
}

export default VehicleDetailsList;
