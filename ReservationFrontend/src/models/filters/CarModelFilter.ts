import { CarCategory } from "../enums/CarCategory";
import { CarSegment } from "../enums/CarSegment";
import { Drivetrain } from "../enums/Drivetrain";
import { EngineType } from "../enums/EngineType";
import { TransmissionType } from "../enums/TransmissionType";

export interface CarModelFilter {
  brand?: string;
  model?: string;
  year?: number;
  segment?: CarSegment;
  category?: CarCategory;
  engineType?: EngineType;
  transmissionType?: TransmissionType;
  drivetrain?: Drivetrain;
  minRentalPrice?: number;
  maxRentalPrice?: number;
}
