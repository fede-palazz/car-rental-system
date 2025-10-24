import { ThemeToggler } from "@/components/ThemeToggler";
import { SidebarInset, SidebarTrigger } from "@/components/ui/sidebar";
import Map from "../components/Map/Map";

function TrackingPage() {
  return (
    <>
      <SidebarInset
        id="sidebar-inset"
        className="p-2 flex flex-col w-full overflow-x-auto">
        <div className=" flex items-center justify-between border-b mb-2">
          <SidebarTrigger />
          {
            <h1 className=" p-2 pb-3 text-3xl font-bold tracking-tight first:mt-0">{`Tracking`}</h1>
          }
          <ThemeToggler></ThemeToggler>
        </div>
        <div className="grow flex flex-col">
          <div className="flex-grow ">
            <Map></Map>
          </div>
        </div>
      </SidebarInset>
    </>
  );
}

export default TrackingPage;
