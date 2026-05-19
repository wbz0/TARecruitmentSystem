import { Outlet, Link, useLocation } from "react-router";
import { User, Briefcase, CheckCircle, LogOut, ArrowLeftRight, Sparkles } from "lucide-react";

export function TALayout() {
  const location = useLocation();

  const navItems = [
    { name: "Jobs", path: "/", icon: Briefcase },
    { name: "Status", path: "/status", icon: CheckCircle },
    { name: "AI Match", path: "/skill-match", icon: Sparkles },
    { name: "Profile", path: "/profile", icon: User },
  ];

  const isActive = (path: string) => {
    if (path === "/") return location.pathname === "/" || location.pathname.startsWith("/job") || location.pathname.startsWith("/apply");
    if (path === "/skill-match") return location.pathname === "/skill-match" || location.pathname === "/missing-skills";
    return location.pathname === path;
  };

  return (
    <div className="flex h-screen w-full bg-[#F5F5F7] font-sans p-3 gap-3">
      {/* Sidebar */}
      <div className="w-56 flex flex-col flex-shrink-0">
        {/* App name */}
        <div className="px-4 pt-5 pb-4">
          <span className="text-[17px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>TA Portal</span>
        </div>

        {/* Nav */}
        <nav className="flex-1 px-2">
          <ul className="space-y-0.5">
            {navItems.map((item) => {
              const active = isActive(item.path);
              return (
                <li key={item.name}>
                  <Link
                    to={item.path}
                    className={`flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all duration-150 ${
                      active
                        ? "bg-white text-[#0071E3] shadow-[0_1px_4px_rgba(0,0,0,0.06)]"
                        : "text-[#86868B] hover:bg-white/70 hover:text-[#1D1D1F]"
                    }`}
                    style={{ fontWeight: 500 }}
                  >
                    <item.icon
                      className="w-[18px] h-[18px] flex-shrink-0"
                      strokeWidth={active ? 2 : 1.75}
                    />
                    <span className="text-[14px]">{item.name}</span>
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>

        {/* Bottom actions */}
        <div className="px-2 pb-4 space-y-0.5">
          <div className="h-px bg-[#E5E5EA] mx-2 mb-2" />
          <Link
            to="/mo"
            className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-[#86868B] hover:bg-white/70 hover:text-[#1D1D1F] transition-all"
            style={{ fontWeight: 500 }}
          >
            <ArrowLeftRight className="w-[18px] h-[18px]" strokeWidth={1.75} />
            <span className="text-[14px]">Switch to MO</span>
          </Link>
          <Link
            to="/login"
            className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-[#86868B] hover:bg-white/70 hover:text-[#FF3B30] transition-all"
            style={{ fontWeight: 500 }}
          >
            <LogOut className="w-[18px] h-[18px]" strokeWidth={1.75} />
            <span className="text-[14px]">Sign Out</span>
          </Link>
        </div>
      </div>

      {/* Main panel */}
      <div className="flex-1 flex flex-col min-w-0 bg-white rounded-2xl shadow-[0_1px_8px_rgba(0,0,0,0.04)] border border-[#E5E5EA]/60 overflow-hidden">
        {/* Top bar */}
        <header className="h-14 flex items-center justify-between px-7 bg-white/80 backdrop-blur-xl border-b border-[#F0F0F5] flex-shrink-0 sticky top-0 z-10">
          <div className="flex items-center gap-3">
            <div className="w-7 h-7 rounded-full bg-gradient-to-br from-[#C7D2FE] to-[#A5B4FC] flex items-center justify-center">
              <User className="w-3.5 h-3.5 text-white" strokeWidth={2} />
            </div>
            <span className="text-[13px] text-[#1D1D1F]" style={{ fontWeight: 500 }}>Sarah Jenkins</span>
          </div>
          <Link
            to="/login"
            className="text-[13px] text-[#86868B] hover:text-[#1D1D1F] transition-colors"
            style={{ fontWeight: 500 }}
          >
            Sign Out
          </Link>
        </header>

        {/* Content */}
        <main className="flex-1 overflow-y-auto bg-white">
          <Outlet />
        </main>
      </div>
    </div>
  );
}