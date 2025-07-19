import { CarModel } from "@/models/CarModel";
import { HoverCard, HoverCardContent, HoverCardTrigger } from "./ui/hover-card";
import { useEffect } from "react";
import { CarFeature } from "@/models/CarFeature";
import CarModelAPI from "@/API/CarModelsAPI";

function CarModelDetailsList({
  model,
  form = false,
}: {
  model: CarModel | undefined;
  form?: boolean;
}) {
  useEffect(() => {
    if (!form || !model?.features[0].description) return;
    model.features.forEach((feature: CarFeature, index) =>
      CarModelAPI.getCarFeatureById(feature.id)
        .then((feat) => {
          model.features[index].description = feat.description;
        })
        .catch((err) => {
          console.error(err);
        })
    );
  }, []);

  return (
    model && (
      <ul className="mt-4 grid grid-cols-3 gap-y-4 w-full justify-items-center text-md">
        <li className="w-full grid grid-cols-3 gap-x-4 ">
          <div className="flex justify-end items-center  col-span-1">
            <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
              brand_family
            </span>
          </div>
          <div className="space-y-1 text-start col-span-2">
            <p>
              <strong className="text-accent-foreground text-lg">Brand</strong>
            </p>
            {model.brand}
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
            {model.model}
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
            {model.year}
          </div>
        </li>
        <li className="w-full grid grid-cols-3 gap-x-4 ">
          <div className="flex justify-end items-center col-span-1">
            <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
              commute
            </span>
          </div>
          <div className="space-y-1 text-start col-span-2">
            <p>
              <strong className="text-accent-foreground text-lg">
                Segment
              </strong>
            </p>
            {model.segment.charAt(0).toUpperCase() +
              model.segment.slice(1).toLowerCase()}
          </div>
        </li>
        <li className="w-full grid grid-cols-3 gap-x-4 ">
          <div className="flex justify-end items-center col-span-1">
            <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
              door_open
            </span>
          </div>
          <div className="space-y-1 text-start col-span-2">
            <p>
              <strong className="text-accent-foreground text-lg">Doors</strong>
            </p>
            {model.doorsNumber}
          </div>
        </li>
        <li className="w-full grid grid-cols-3 gap-x-4 ">
          <div className="flex justify-end items-center col-span-1">
            <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
              chair
            </span>
          </div>
          <div className="space-y-1 text-start col-span-2">
            <p>
              <strong className="text-accent-foreground text-lg">Seats</strong>
            </p>
            {model.seatingCapacity}
          </div>
        </li>
        <li className="w-full grid grid-cols-3 gap-x-4 ">
          <div className="flex justify-end items-center col-span-1">
            <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
              luggage
            </span>
          </div>
          <div className="text-start space-y-1  col-span-2">
            <p>
              <strong className="text-accent-foreground text-lg">
                Luggage Capacity
              </strong>
            </p>
            {model.luggageCapacity}
          </div>
        </li>
        <li className="w-full grid grid-cols-3 gap-x-4 ">
          <div className="flex justify-end items-center  col-span-1">
            <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
              category
            </span>
          </div>
          <div className="text-start col-span-2 space-y-1 ">
            <p>
              <strong className="text-accent-foreground text-lg">
                Category
              </strong>
            </p>
            {model.category.charAt(0).toUpperCase() +
              model.category.slice(1).toLowerCase()}
          </div>
        </li>
        <li className="w-full grid grid-cols-3 gap-x-4 ">
          <div className="flex justify-end items-center  col-span-1">
            <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
              extension
            </span>
          </div>
          <div className="space-y-1 text-start  col-span-2">
            <p>
              <strong className="text-accent-foreground text-lg">
                Features
              </strong>
            </p>
            <HoverCard>
              <HoverCardTrigger>
                <span className="text-foreground hover:text-muted-foreground ">
                  {model?.features.length}
                  {model?.features.length > 0 && (
                    <span className="material-symbols-outlined md-18 align-middle">
                      arrow_drop_down
                    </span>
                  )}
                </span>
              </HoverCardTrigger>
              <HoverCardContent className="w-fit">
                <ul className="my-2 ml-4 list-disc [&>li]:mt-2">
                  {model.features.map((feature) => (
                    <li key={feature.id}>{feature.description}</li>
                  ))}
                </ul>
              </HoverCardContent>
            </HoverCard>
          </div>
        </li>
        <li className="w-full grid grid-cols-3 gap-x-4 ">
          <div className="flex justify-end items-center  col-span-1">
            <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
              car_gear
            </span>
          </div>
          <div className="space-y-1 text-start  col-span-2">
            <p>
              <strong className="text-accent-foreground text-lg">
                Engine Type
              </strong>
            </p>
            {model.engineType.charAt(0).toUpperCase() +
              model.engineType.slice(1).toLowerCase()}
          </div>
        </li>
        <li className="w-full grid grid-cols-3 gap-x-4 ">
          <div className="flex justify-end items-center  col-span-1">
            <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
              auto_transmission
            </span>
          </div>
          <div className="space-y-1 text-start  col-span-2">
            <p>
              <strong className="text-accent-foreground text-lg">
                Transmission Type
              </strong>
            </p>
            {model.transmissionType.charAt(0).toUpperCase() +
              model.transmissionType.slice(1).toLowerCase()}
          </div>
        </li>
        <li className="w-full grid grid-cols-3 gap-x-4 ">
          <div className="flex justify-end items-center  col-span-1">
            <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
              component_exchange
            </span>
          </div>
          <div className="space-y-1 text-start  col-span-2">
            <p>
              <strong className="text-accent-foreground text-lg">
                Drivetrain
              </strong>
            </p>
            {model.drivetrain.charAt(0).toUpperCase() +
              model.drivetrain.slice(1).toLowerCase()}
          </div>
        </li>
        {form ? (
          <div className="col-span-full grid grid-cols-2">
            <li className="w-full grid grid-cols-3 gap-x-4 ">
              <div className="flex justify-end items-center  col-span-1">
                <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
                  payments
                </span>
              </div>
              <div className="space-y-1 text-start col-span-2">
                <p>
                  <strong className="text-accent-foreground text-lg">
                    Rental Price
                  </strong>
                </p>
                {model.rentalPrice}
              </div>
            </li>
            {model.motorDisplacement ? (
              <li className="w-full grid grid-cols-3 gap-x-4 ">
                <div className="flex justify-end items-center col-span-1">
                  <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
                    speed
                  </span>
                </div>
                <div className="space-y-1 text-start col-span-2">
                  <p>
                    <strong className="text-accent-foreground text-lg">
                      Motor Displacement
                    </strong>
                  </p>
                  {model.motorDisplacement} cc
                </div>
              </li>
            ) : (
              <li className="w-full grid grid-cols-3 gap-x-4 "></li>
            )}
          </div>
        ) : (
          model.motorDisplacement && (
            <>
              <li className="w-full grid grid-cols-3 gap-x-4 "></li>
              <li className="w-full grid grid-cols-3 gap-x-4 ">
                <div className="flex justify-end items-center col-span-1">
                  <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
                    speed
                  </span>
                </div>
                <div className="space-y-1 text-start col-span-2">
                  <p>
                    <strong className="text-accent-foreground text-lg">
                      Motor Displacement
                    </strong>
                  </p>
                  {model.motorDisplacement} cc
                </div>
              </li>
              <li className="w-full grid grid-cols-3 gap-x-4 "></li>
            </>
          )
        )}
      </ul>
    )
  );
}

export default CarModelDetailsList;
