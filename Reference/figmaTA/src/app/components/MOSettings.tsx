import { useState } from "react";
import { Bell, Lock, User, Globe } from "lucide-react";

const inputClass = "w-full bg-white border border-[#E5E5EA] rounded-xl px-4 py-3 text-[15px] text-[#1D1D1F] focus:border-[#0071E3]/40 focus:ring-4 focus:ring-[#0071E3]/8 outline-none transition-all";
const disabledInputClass = "w-full bg-[#F5F5F7] border border-[#E5E5EA] rounded-xl px-4 py-3 text-[15px] text-[#AEAEB2] outline-none cursor-not-allowed";

function Toggle({ id, defaultChecked }: { id: string; defaultChecked?: boolean }) {
  const [on, setOn] = useState(defaultChecked ?? false);
  return (
    <button
      type="button"
      role="switch"
      aria-checked={on}
      onClick={() => setOn(!on)}
      className={`relative w-[46px] h-[27px] rounded-full transition-colors duration-200 focus:outline-none ${on ? "bg-[#34C759]" : "bg-[#D1D1D6]"}`}
      id={id}
    >
      <span
        className={`absolute top-[2px] left-[2px] w-[23px] h-[23px] bg-white rounded-full shadow-[0_1px_3px_rgba(0,0,0,0.2)] transition-transform duration-200 ${on ? "translate-x-[19px]" : "translate-x-0"}`}
      />
    </button>
  );
}

const tabs = [
  { name: "Account", icon: User },
  { name: "Notifications", icon: Bell },
  { name: "Security", icon: Lock },
  { name: "Integration", icon: Globe },
];

export function MOSettings() {
  const [activeTab, setActiveTab] = useState("Account");

  return (
    <div className="p-8 max-w-2xl mx-auto">
      {/* Header */}
      <div className="mb-7">
        <h2 className="text-[26px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>Settings</h2>
        <p className="text-[15px] text-[#86868B] mt-1">Manage your preferences and portal configuration.</p>
      </div>

      {/* Tab strip */}
      <div className="flex items-center gap-1 bg-[#F5F5F7] rounded-xl p-1 mb-6">
        {tabs.map(({ name, icon: Icon }) => (
          <button
            key={name}
            onClick={() => setActiveTab(name)}
            className={`flex items-center gap-1.5 px-4 py-2 rounded-lg text-[13px] flex-1 justify-center transition-all ${
              activeTab === name
                ? "bg-white text-[#1D1D1F] shadow-[0_1px_4px_rgba(0,0,0,0.08)]"
                : "text-[#86868B] hover:text-[#1D1D1F]"
            }`}
            style={{ fontWeight: 500 }}
          >
            <Icon className="w-3.5 h-3.5" strokeWidth={1.75} />
            <span className="hidden sm:inline">{name}</span>
          </button>
        ))}
      </div>

      {/* Account tab content */}
      {activeTab === "Account" && (
        <div className="space-y-5">
          {/* Dept info */}
          <div className="bg-[#F5F5F7] rounded-3xl p-7 space-y-5">
            <p className="text-[12px] text-[#86868B] uppercase tracking-widest" style={{ fontWeight: 500 }}>Department Details</p>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="flex flex-col gap-1.5">
                <label className="text-[12px] text-[#86868B] uppercase tracking-wide ml-1" style={{ fontWeight: 500 }}>Department</label>
                <input type="text" defaultValue="Computer Science" className={disabledInputClass} disabled />
              </div>
              <div className="flex flex-col gap-1.5">
                <label className="text-[12px] text-[#86868B] uppercase tracking-wide ml-1" style={{ fontWeight: 500 }}>Primary Contact</label>
                <input type="text" defaultValue="Michael Scott" className={inputClass} />
              </div>
              <div className="flex flex-col gap-1.5 md:col-span-2">
                <label className="text-[12px] text-[#86868B] uppercase tracking-wide ml-1" style={{ fontWeight: 500 }}>Contact Email</label>
                <input type="email" defaultValue="hiring.cs@university.edu" className={inputClass} />
              </div>
            </div>
          </div>

          {/* Hiring prefs */}
          <div className="bg-[#F5F5F7] rounded-3xl p-7 space-y-4">
            <p className="text-[12px] text-[#86868B] uppercase tracking-widest" style={{ fontWeight: 500 }}>Hiring Preferences</p>

            <div className="bg-white rounded-2xl px-5 py-4 flex items-center justify-between">
              <div className="pr-4">
                <p className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 500 }}>Auto-Reject Incomplete Applications</p>
                <p className="text-[13px] text-[#86868B] mt-0.5">Filter out applications missing required documents automatically.</p>
              </div>
              <Toggle id="toggle1" defaultChecked />
            </div>

            <div className="bg-white rounded-2xl px-5 py-4 flex items-center justify-between">
              <div className="pr-4">
                <p className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 500 }}>Daily Email Digest</p>
                <p className="text-[13px] text-[#86868B] mt-0.5">Receive a daily summary of new applications.</p>
              </div>
              <Toggle id="toggle2" />
            </div>
          </div>

          {/* Save */}
          <div className="flex justify-end">
            <button
              type="button"
              className="px-7 py-2.5 bg-[#0071E3] text-white rounded-full text-[14px] hover:bg-[#0077ED] transition-all active:scale-95 shadow-[0_2px_12px_rgba(0,113,227,0.28)]"
              style={{ fontWeight: 500 }}
            >
              Save Preferences
            </button>
          </div>
        </div>
      )}

      {/* Placeholder for other tabs */}
      {activeTab !== "Account" && (
        <div className="bg-[#F5F5F7] rounded-3xl p-12 flex flex-col items-center text-center">
          <div className="w-12 h-12 bg-white rounded-2xl flex items-center justify-center shadow-sm mb-4">
            {(() => {
              const tab = tabs.find((t) => t.name === activeTab);
              if (!tab) return null;
              const Icon = tab.icon;
              return <Icon className="w-5 h-5 text-[#AEAEB2]" strokeWidth={1.75} />;
            })()}
          </div>
          <p className="text-[15px] text-[#86868B]">{activeTab} settings coming soon.</p>
        </div>
      )}
    </div>
  );
}
