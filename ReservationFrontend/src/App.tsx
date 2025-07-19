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
import AddOrEditReservationDialog from "./components/Forms/Reservation/AddOrEditReservationDialog.tsx";
import SetActualPickupDateDialog from "./components/Forms/Reservation/SetActualPickupDateDialog.tsx";
import FinalizeReservationDialog from "./components/Forms/Reservation/FinalizeReservationDialog.tsx";
import AddOrEditMaintenanceDialog from "./components/Forms/Maintenance/AddOrEditMaintenanceDialog.tsx";
import AddOrEditNoteDialog from "./components/Forms/Note/AddOrEditNoteDialog.tsx";
import { LandingPage } from "./pages/LandingPage.tsx";
import { DateRange } from "react-day-picker";
import UserAPI from "./API/UserAPI.ts";
import { toast } from "sonner";
import { setCsrfToken } from "./API/csrfToken.ts";

function App() {
  const [user, setUser] = useState<User | undefined>(undefined);
  const [availabilityDates, setAvailabilityDates] = useState<
    DateRange | undefined
  >(undefined);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    if (params.has("logout")) {
      toast.success("Logout successfull");
      setCsrfToken("");
      return;
    }
    UserAPI.getLoggedUserInfo()
      .then((res) => {
        setUser(res);
        setCsrfToken(res.csrf);
      })
      .catch((err) => {
        toast.error(err.message);
        setUser(undefined);
        setCsrfToken("");
      });
  }, []);

  return (
    <UserContext.Provider value={user}>
      <ThemeProvider defaultTheme="dark" storageKey="vite-ui-theme">
        <Toaster richColors closeButton />
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
                  user && user.role == UserRole.CUSTOMER ? (
                    <Navigate to="/models"></Navigate>
                  ) : (
                    <AddOrEditModelDialog />
                  )
                }></Route>
              <Route
                path="edit"
                element={
                  user && user.role == UserRole.CUSTOMER ? (
                    <Navigate to="/models"></Navigate>
                  ) : (
                    <AddOrEditModelDialog />
                  )
                }></Route>
              <Route
                path="reserve"
                element={<AddOrEditReservationDialog />}></Route>
            </Route>
            <Route
              path={"/models/:id"}
              element={<ModelDetailsPage date={availabilityDates} />}>
              <Route
                path="edit"
                element={
                  user && user.role == UserRole.CUSTOMER ? (
                    <Navigate to="/models/:id"></Navigate>
                  ) : (
                    <AddOrEditModelDialog />
                  )
                }></Route>
              <Route
                path="reserve"
                element={<AddOrEditReservationDialog />}></Route>
            </Route>
            <Route
              path={"/vehicles"}
              element={
                user && user.role == UserRole.CUSTOMER ? (
                  <Navigate to="/models"></Navigate>
                ) : (
                  <VehiclesPage />
                )
              }>
              <Route path="add" element={<AddOrEditVehicleDialog />}></Route>
              <Route path="edit" element={<AddOrEditVehicleDialog />}></Route>
            </Route>
            <Route
              path={"vehicles/:vehicleId"}
              element={<VehicleDetailsPage />}>
              <Route path="edit" element={<AddOrEditVehicleDialog />}></Route>
              <Route
                path="add-maintenance"
                element={<AddOrEditMaintenanceDialog />}></Route>
              <Route
                path="edit-maintenance/:maintenanceId"
                element={<AddOrEditMaintenanceDialog />}></Route>
              <Route path="add-note" element={<AddOrEditNoteDialog />}></Route>
              <Route
                path="edit-note/:noteId"
                element={<AddOrEditNoteDialog />}></Route>
            </Route>
            <Route
              path={"/reservations"}
              element={<ReservationsPage />}>
              <Route
                path="pick-up-date"
                element={
                  user && user.role == UserRole.CUSTOMER ? (
                    <Navigate to="/reservations"></Navigate>
                  ) : (
                    <SetActualPickupDateDialog />
                  )
                }></Route>
              <Route
                path="finalize"
                element={
                  user && user.role == UserRole.CUSTOMER ? (
                    <Navigate to="/reservations"></Navigate>
                  ) : (
                    <FinalizeReservationDialog />
                  )
                }></Route>
            </Route>
          </Route>
        </Routes>
      </ThemeProvider>
    </UserContext.Provider>
  );
}

export default App;
