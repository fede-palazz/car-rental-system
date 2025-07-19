import { CarSegment } from "@/models/enums/CarSegment.ts";
import { EngineType } from "@/models/enums/EngineType.ts";
import { TransmissionType } from "@/models/enums/TransmissionType.ts";
import { Drivetrain } from "@/models/enums/Drivetrain.ts";
import { CarCategory } from "../enums/CarCategory";

export interface CarModelCreateDTO {
  brand: string;
  model: string;
  year: string;
  segment: CarSegment;
  doorsNumber: number;
  seatingCapacity: number;
  luggageCapacity: number;
  category: CarCategory;
  featureIds: number[];
  engineType: EngineType;
  transmissionType: TransmissionType;
  drivetrain: Drivetrain;
  motorDisplacement?: number;
  rentalPrice: number;
}
