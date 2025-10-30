import { ThemeProvider } from "@/components/ThemeProvider";
import { SidebarProvider } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/Sidebars/AppSidebar.tsx";
import { Navigate, Outlet, Route, Routes } from "react-router-dom";
import CarModelsPage from "./pages/CarModelsPage.tsx";
import VehiclesPage from "@/pages/VehiclesPage.tsx";
import VehicleDetailsPage from "./pages/VehicleDetailsPage.tsx";
import ModelDetailsPage from "./pages/ModelDetailsPage.tsx";
import AddOrEditModelDialog from "./components/Forms/CarModel/AddOrEditModelDialog.tsx";
import AddOrEditVehicleDialog from "./components/Forms/Vehicle/AddOrEditVehicleDialog.tsx";
import UserContext from "./contexts/UserContext.tsx";
import { User } from "./models/User.ts";
import { useEffect, useState } from "react";
import { Toaster } from "./components/ui/sonner.tsx";
import { UserRole } from "./models/enums/UserRole.ts";
import ReservationsPage from "./pages/ReservationsPage.tsx";
import SetActualPickupDateDialog from "./components/Forms/Reservation/SetActualPickupDateDialog.tsx";
import FinalizeReservationDialog from "./components/Forms/Reservation/FinalizeReservationDialog.tsx";
import AddOrEditMaintenanceDialog from "./components/Forms/Maintenance/AddOrEditMaintenanceDialog.tsx";
import AddOrEditNoteDialog from "./components/Forms/Note/AddOrEditNoteDialog.tsx";
import { LandingPage } from "./pages/LandingPage.tsx";
import { DateRange } from "react-day-picker";
import UserAPI from "./API/UserAPI.ts";
import { toast } from "sonner";
import { setCsrfToken } from "./API/csrfToken.ts";
import ChangeVehicleOrDeleteReservationDialog from "./components/Forms/Reservation/ChangeVehicleOrDeleteReservationDialog.tsx";
import { Spinner } from "./components/ui/spinner.tsx";
import DeleteReservationDialog from "./components/Forms/Reservation/DeleteReservationDialog.tsx";
import AddReservationDialog from "./components/Forms/Reservation/AddReservationDialog.tsx";
import DeleteCarModelDialog from "./components/Forms/CarModel/DeleteCarModelDialog.tsx";
import DeleteVehicleDialog from "./components/Forms/Vehicle/DeleteVehicleDialog.tsx";
import TrackingPage from "./pages/TrackingPage.tsx";
import MapVehicleCard from "./components/Map/MapVehicleCard.tsx";
import AnalyticsPage from "./pages/AnalyticsPage.tsx";
import DeleteMaintenanceDialog from "@/components/Forms/Maintenance/DeleteMaintenanceDialog.tsx";
import FinalizeMaintenanceDialog from "./components/Forms/Maintenance/FinalizeMaintenanceDialog.tsx";

function App() {
  const [user, setUser] = useState<User | undefined>(undefined);
  const [fetchingUser, setFetchingUser] = useState<boolean>(true);

  const [availabilityDates, setAvailabilityDates] = useState<
    DateRange | undefined
  >(undefined);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    if (params.has("logout")) {
      toast.success("Logout successfull");
      setCsrfToken("");
      setFetchingUser(false);
      return;
    }
    setFetchingUser(true);
    UserAPI.getLoggedUserInfo()
      .then((res) => {
        toast.success("Login successfull");
        setUser(res);
        setCsrfToken(res.csrf);
      })
      .catch((err: Error) => {
        toast.error(err.message);
        setUser(undefined);
        setCsrfToken("");
      })
      .finally(() => {
        setFetchingUser(false);
      });
  }, []);

  return (
    <UserContext.Provider value={user}>
      <ThemeProvider defaultTheme="dark" storageKey="vite-ui-theme">
        <Toaster richColors closeButton />
        {fetchingUser ? (
          <Spinner></Spinner>
        ) : (
          <Routes>
            <Route
              path="/"
              element={
                user && user.role != UserRole.CUSTOMER ? (
                  <Navigate to="/models"></Navigate>
                ) : (
                  <>
                    <LandingPage
                      date={availabilityDates}
                      setDate={setAvailabilityDates}
                    />
                  </>
                )
              }></Route>
            <Route
              path="/"
              element={
                <SidebarProvider>
                  <AppSidebar variant="inset" setUser={setUser} />
                  <Outlet></Outlet>
                </SidebarProvider>
              }>
              <Route
                path={"/models"}
                element={
                  <CarModelsPage
                    date={availabilityDates}
                    setDate={setAvailabilityDates}
                  />
                }>
                <Route
                  path="add"
                  element={
                    user && user.role != UserRole.FLEET_MANAGER ? (
                      <Navigate to="/models"></Navigate>
                    ) : (
                      <AddOrEditModelDialog />
                    )
                  }></Route>
                <Route
                  path="edit/:carModelId"
                  element={
                    user && user.role == UserRole.CUSTOMER ? (
                      <Navigate to="/models"></Navigate>
                    ) : (
                      <AddOrEditModelDialog />
                    )
                  }></Route>
                <Route
                  path="reserve/:carModelId"
                  element={
                    <AddReservationDialog
                      plannedPickUpDate={availabilityDates?.from}
                      plannedDropOffDate={availabilityDates?.to}
                    />
                  }></Route>
                <Route
                  path="delete/:carModelId"
                  element={
                    user && user.role !== UserRole.CUSTOMER ? (
                      <DeleteCarModelDialog />
                    ) : (
                      <Navigate to="/models"></Navigate>
                    )
                  }></Route>
              </Route>
              <Route
                path={"/models/:carModelId"}
                element={<ModelDetailsPage />}>
                <Route
                  path="edit"
                  element={
                    user && user.role == UserRole.CUSTOMER ? (
                      <Navigate to="/models/:carModelId"></Navigate>
                    ) : (
                      <AddOrEditModelDialog />
                    )
                  }></Route>
                <Route
                  path="delete"
                  element={
                    user && user.role !== UserRole.CUSTOMER ? (
                      <DeleteCarModelDialog />
                    ) : (
                      <Navigate to="/models/:carModelId"></Navigate>
                    )
                  }></Route>
                <Route
                  path="reserve"
                  element={
                    <AddReservationDialog
                      plannedPickUpDate={availabilityDates?.from}
                      plannedDropOffDate={availabilityDates?.to}
                    />
                  }></Route>
              </Route>
              <Route
                path={"/vehicles"}
                element={
                  !user || (user && user.role == UserRole.CUSTOMER) ? (
                    <Navigate to="/models"></Navigate>
                  ) : (
                    <VehiclesPage />
                  )
                }>
                <Route
                  path="add"
                  element={
                    user && user.role == UserRole.CUSTOMER ? (
                      <Navigate to="/models"></Navigate>
                    ) : (
                      <AddOrEditVehicleDialog />
                    )
                  }></Route>
                <Route
                  path="edit/:vehicleId"
                  element={
                    user && user.role == UserRole.CUSTOMER ? (
                      <Navigate to="/models"></Navigate>
                    ) : (
                      <AddOrEditVehicleDialog />
                    )
                  }></Route>
                <Route
                  path="delete/:vehicleId"
                  element={
                    user && user.role == UserRole.CUSTOMER ? (
                      <Navigate to="/models"></Navigate>
                    ) : (
                      <DeleteVehicleDialog />
                    )
                  }></Route>
              </Route>
              <Route
                path={"vehicles/:vehicleId"}
                element={<VehicleDetailsPage />}>
                <Route
                  path="edit"
                  element={
                    user && user.role == UserRole.CUSTOMER ? (
                      <Navigate to="/models"></Navigate>
                    ) : (
                      <AddOrEditVehicleDialog />
                    )
                  }></Route>
                <Route
                  path="delete"
                  element={
                    user && user.role == UserRole.CUSTOMER ? (
                      <Navigate to="/models"></Navigate>
                    ) : (
                      <DeleteVehicleDialog />
                    )
                  }></Route>
                <Route
                  path="add-maintenance"
                  element={
                    user && user.role == UserRole.CUSTOMER ? (
                      <Navigate to="/models"></Navigate>
                    ) : (
                      <AddOrEditMaintenanceDialog />
                    )
                  }></Route>
                <Route
                  path="edit-maintenance/:maintenanceId"
                  element={
                    user && user.role == UserRole.CUSTOMER ? (
                      <Navigate to="/models"></Navigate>
                    ) : (
                      <AddOrEditMaintenanceDialog />
                    )
                  }></Route>
                <Route
                  path="finalize-maintenance/:maintenanceId"
                  element={
                    user && user.role == UserRole.CUSTOMER ? (
                      <Navigate to="/models"></Navigate>
                    ) : (
                      <FinalizeMaintenanceDialog />
                    )
                  }></Route>
                <Route
                  path="delete-maintenance/:maintenanceId"
                  element={
                    user && user.role == UserRole.CUSTOMER ? (
                      <Navigate to="/models"></Navigate>
                    ) : (
                      <DeleteMaintenanceDialog />
                    )
                  }></Route>
                <Route
                  path="add-note"
                  element={
                    user && user.role == UserRole.CUSTOMER ? (
                      <Navigate to="/models"></Navigate>
                    ) : (
                      <AddOrEditNoteDialog />
                    )
                  }></Route>
                <Route
                  path="edit-note/:noteId"
                  element={
                    user && user.role == UserRole.CUSTOMER ? (
                      <Navigate to="/models"></Navigate>
                    ) : (
                      <AddOrEditNoteDialog />
                    )
                  }></Route>
                <Route
                  path="delete-note/:noteId"
                  element={
                    user && user.role == UserRole.CUSTOMER ? (
                      <Navigate to="/models"></Navigate>
                    ) : (
                      <AddOrEditNoteDialog />
                    )
                  }></Route>
              </Route>
              <Route
                path={"/reservations"}
                element={
                  fetchingUser && !user ? (
                    <Spinner></Spinner>
                  ) : user ? (
                    <ReservationsPage />
                  ) : (
                    <Navigate to="/"></Navigate>
                  )
                }>
                <Route
                  path="pick-up-date/:reservationId"
                  element={
                    user && user.role == UserRole.CUSTOMER ? (
                      <Navigate to="/reservations"></Navigate>
                    ) : (
                      <SetActualPickupDateDialog />
                    )
                  }></Route>
                <Route
                  path="change-vehicle/:reservationId"
                  element={
                    user && user.role == UserRole.CUSTOMER ? (
                      <Navigate to="/reservations"></Navigate>
                    ) : (
                      <ChangeVehicleOrDeleteReservationDialog />
                    )
                  }></Route>
                <Route
                  path="delete/:reservationId"
                  element={<DeleteReservationDialog />}></Route>
                <Route
                  path="finalize/:reservationId"
                  element={
                    user && user.role == UserRole.CUSTOMER ? (
                      <Navigate to="/reservations"></Navigate>
                    ) : (
                      <FinalizeReservationDialog />
                    )
                  }></Route>
              </Route>
              <Route
                path="/tracking"
                element={
                  fetchingUser && !user ? (
                    <Spinner></Spinner>
                  ) : user ? (
                    <TrackingPage />
                  ) : (
                    <Navigate to="/"></Navigate>
                  )
                }>
                <Route path=":vehicleId" element={<MapVehicleCard />}></Route>
              </Route>
              <Route
                path="/analytics"
                element={
                  fetchingUser && !user ? (
                    <Spinner></Spinner>
                  ) : user ? (
                    <AnalyticsPage />
                  ) : (
                    <Navigate to="/"></Navigate>
                  )
                }></Route>
            </Route>
          </Routes>
        )}
      </ThemeProvider>
    </UserContext.Provider>
  );
}

export default App;
