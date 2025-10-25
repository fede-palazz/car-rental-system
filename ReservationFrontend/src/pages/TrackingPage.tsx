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
          <div id="map" className="flex-grow relative">
            <Map></Map>
            <div className="absolute top-10 right-2 -translate-x-1/2 -translate-y-1/2 flex items-center justify-center z-10000">
              <div className="relative w-9 h-9">
                <div className="absolute inset-0 bg-red-500 rounded-full animate-ping opacity-40" />
                <div className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-4.5 h-4.5 bg-red-600 rounded-full" />
              </div>
            </div>
          </div>
        </div>
      </SidebarInset>
    </>
  );
}

export default TrackingPage;
