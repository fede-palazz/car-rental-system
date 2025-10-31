import { Outlet, useLocation, useNavigate, useParams } from "react-router-dom";
import DefaultCar from "@/assets/defaultCarModel.png";
import { CarModel } from "@/models/CarModel.ts";
import { useContext, useEffect, useState } from "react";
import { Separator } from "@/components/ui/separator";
import { Button } from "@/components/ui/button";
import CarModelAPI from "@/API/CarModelsAPI";
import CarModelDetailsList from "@/components/CarModelDetailsList";
import { SidebarInset, SidebarTrigger } from "@/components/ui/sidebar";
import { ThemeToggler } from "@/components/ThemeToggler";
import { UserRole } from "@/models/enums/UserRole";
import UserContext from "@/contexts/UserContext";
import { toast } from "sonner";

function ModelDetailsPage() {
  const user = useContext(UserContext);
  const navigate = useNavigate();
  const location = useLocation();
  const { carModelId } = useParams<{ carModelId: "string" }>();
  const [model, setModel] = useState<CarModel | undefined>(undefined);

  useEffect(() => {
    if (location.pathname !== `/models/${carModelId}`) return;
    CarModelAPI.getModelById(Number(carModelId))
      .then((model: CarModel) => {
        setModel(model);
      })
      .catch((err: Error) => {
        toast.error(err.message);
      });
  }, [carModelId, location.pathname]);

  return (
    <SidebarInset id="sidebar-inset" className="p-2 flex flex-col w-full">
      <div className=" flex items-center justify-between border-b">
        <SidebarTrigger />
        {model && (
          <h1 className=" p-2 pb-3 text-3xl font-bold tracking-tight first:mt-0">
            {`${model?.brand} ${model?.model} ${model?.year}`}
          </h1>
        )}
        <ThemeToggler></ThemeToggler>
      </div>
      {model && (
        <div className="grow flex flex-col">
          <div className="flex flex-col md:flex-row items-center gap-6 h-full">
            <div className="h-full flex w-1/2 flex-col">
              <div className="mt-3">
                <Button
                  variant="ghost"
                  size="icon"
                  onClick={() => navigate(-1)}>
                  <span className="material-symbols-outlined md-18">
                    arrow_back
                  </span>
                </Button>
              </div>
              <div className="flex-grow flex items-center justify-center">
                <img
                  src={DefaultCar}
                  className="py-4"
                  alt={`${model.brand} ${model.model}`}
                />
              </div>
            </div>
            <Separator
              orientation="vertical"
              className="hidden md:block"></Separator>
            <Separator
              orientation="horizontal"
              className="block md:hidden"></Separator>
            <div className="flex flex-col h-full mt-2 py-2 w-full">
              <h2 className="text-3xl text-center font-extrabold">Details</h2>
              <CarModelDetailsList model={model}></CarModelDetailsList>
              <div className="grid grid-cols-2 items-center w-full gap-4 mt-auto mb-4">
                <p className="text-3xl font-extrabold text-center">
                  {model.rentalPrice.toFixed(2) + " €"}{" "}
                  <span className="text-muted-foreground !text-base">
                    /per day
                  </span>
                </p>
                <div className="flex flex-col gap-4 items-center pt-auto">
                  {user && user.role == UserRole.FLEET_MANAGER ? (
                    <>
                      <Button
                        variant="destructive"
                        size="lg"
                        className="w-1/2"
                        onClick={() => navigate("delete")}>
                        <span className="material-symbols-outlined md-18">
                          delete
                        </span>
                        Delete
                      </Button>
                      <Button
                        variant="default"
                        size="lg"
                        className="w-1/2"
                        onClick={() => navigate("edit")}>
                        <span className="material-symbols-outlined md-18">
                          edit
                        </span>
                        Edit
                      </Button>
                    </>
                  ) : (
                    user &&
                    user.role == UserRole.CUSTOMER && (
                      <Button
                        disabled={!user}
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate("reserve");
                        }}>
                        <span className="material-symbols-outlined md-18">
                          event_upcoming
                        </span>
                        {user ? "Reserve" : "Login to reserve"}
                      </Button>
                    )
                  )}
                </div>
              </div>
            </div>
          </div>
          <Outlet></Outlet>
        </div>
      )}
    </SidebarInset>
  );
}

export default ModelDetailsPage;
