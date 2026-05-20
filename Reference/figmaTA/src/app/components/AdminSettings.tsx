import { useState } from "react";
import { Bell, Shield, Database, Mail, Globe, Save } from "lucide-react";

export function AdminSettings() {
  const [emailNotifications, setEmailNotifications] = useState(true);
  const [autoApprove, setAutoApprove] = useState(false);
  const [maintenanceMode, setMaintenanceMode] = useState(false);
  const [twoFactorAuth, setTwoFactorAuth] = useState(true);

  return (
    <div className="p-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-[28px] text-[#1D1D1F] mb-2" style={{ fontWeight: 600 }}>
          System Settings
        </h1>
        <p className="text-[15px] text-[#86868B]">
          Configure system-wide preferences and security settings
        </p>
      </div>

      <div className="max-w-3xl space-y-6">
        {/* Notifications */}
        <div className="bg-white rounded-2xl border border-[#E5E5EA] p-6">
          <div className="flex items-center gap-3 mb-5">
            <div className="w-9 h-9 bg-blue-500 rounded-lg flex items-center justify-center">
              <Bell className="w-4 h-4 text-white" strokeWidth={2} />
            </div>
            <div>
              <h2 className="text-[17px] text-[#1D1D1F]" style={{ fontWeight: 600 }}>
                Notifications
              </h2>
              <p className="text-[13px] text-[#86868B]">
                Manage system notification preferences
              </p>
            </div>
          </div>

          <div className="space-y-4">
            <div className="flex items-center justify-between py-3 border-b border-[#E5E5EA]">
              <div>
                <div className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 500 }}>
                  Email Notifications
                </div>
                <div className="text-[13px] text-[#86868B]">
                  Send email updates for important events
                </div>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={emailNotifications}
                  onChange={(e) => setEmailNotifications(e.target.checked)}
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
              </label>
            </div>

            <div className="flex items-center justify-between py-3">
              <div>
                <div className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 500 }}>
                  Auto-approve New Users
                </div>
                <div className="text-[13px] text-[#86868B]">
                  Automatically approve new user registrations
                </div>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={autoApprove}
                  onChange={(e) => setAutoApprove(e.target.checked)}
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
              </label>
            </div>
          </div>
        </div>

        {/* Security */}
        <div className="bg-white rounded-2xl border border-[#E5E5EA] p-6">
          <div className="flex items-center gap-3 mb-5">
            <div className="w-9 h-9 bg-purple-500 rounded-lg flex items-center justify-center">
              <Shield className="w-4 h-4 text-white" strokeWidth={2} />
            </div>
            <div>
              <h2 className="text-[17px] text-[#1D1D1F]" style={{ fontWeight: 600 }}>
                Security
              </h2>
              <p className="text-[13px] text-[#86868B]">
                Configure security and authentication settings
              </p>
            </div>
          </div>

          <div className="space-y-4">
            <div className="flex items-center justify-between py-3 border-b border-[#E5E5EA]">
              <div>
                <div className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 500 }}>
                  Two-Factor Authentication
                </div>
                <div className="text-[13px] text-[#86868B]">
                  Require 2FA for admin accounts
                </div>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={twoFactorAuth}
                  onChange={(e) => setTwoFactorAuth(e.target.checked)}
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-purple-300 rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-purple-600"></div>
              </label>
            </div>

            <div className="flex items-center justify-between py-3">
              <div>
                <div className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 500 }}>
                  Session Timeout
                </div>
                <div className="text-[13px] text-[#86868B]">
                  Auto logout after inactivity
                </div>
              </div>
              <select className="px-4 py-2 bg-[#F5F5F7] border border-[#E5E5EA] rounded-lg text-[14px] text-[#1D1D1F] focus:outline-none focus:border-[#0071E3]">
                <option>30 minutes</option>
                <option>1 hour</option>
                <option>2 hours</option>
                <option>Never</option>
              </select>
            </div>
          </div>
        </div>

        {/* System */}
        <div className="bg-white rounded-2xl border border-[#E5E5EA] p-6">
          <div className="flex items-center gap-3 mb-5">
            <div className="w-9 h-9 bg-orange-500 rounded-lg flex items-center justify-center">
              <Database className="w-4 h-4 text-white" strokeWidth={2} />
            </div>
            <div>
              <h2 className="text-[17px] text-[#1D1D1F]" style={{ fontWeight: 600 }}>
                System
              </h2>
              <p className="text-[13px] text-[#86868B]">
                System maintenance and data management
              </p>
            </div>
          </div>

          <div className="space-y-4">
            <div className="flex items-center justify-between py-3 border-b border-[#E5E5EA]">
              <div>
                <div className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 500 }}>
                  Maintenance Mode
                </div>
                <div className="text-[13px] text-[#86868B]">
                  Temporarily disable access for maintenance
                </div>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={maintenanceMode}
                  onChange={(e) => setMaintenanceMode(e.target.checked)}
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-orange-300 rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-orange-600"></div>
              </label>
            </div>

            <div className="flex items-center justify-between py-3">
              <div>
                <div className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 500 }}>
                  Data Backup
                </div>
                <div className="text-[13px] text-[#86868B]">
                  Last backup: March 18, 2026 at 11:30 PM
                </div>
              </div>
              <button className="px-4 py-2 bg-[#F5F5F7] border border-[#E5E5EA] rounded-lg text-[14px] text-[#1D1D1F] hover:bg-[#E5E5EA] transition-colors" style={{ fontWeight: 500 }}>
                Backup Now
              </button>
            </div>
          </div>
        </div>

        {/* Email Server */}
        <div className="bg-white rounded-2xl border border-[#E5E5EA] p-6">
          <div className="flex items-center gap-3 mb-5">
            <div className="w-9 h-9 bg-green-500 rounded-lg flex items-center justify-center">
              <Mail className="w-4 h-4 text-white" strokeWidth={2} />
            </div>
            <div>
              <h2 className="text-[17px] text-[#1D1D1F]" style={{ fontWeight: 600 }}>
                Email Server
              </h2>
              <p className="text-[13px] text-[#86868B]">
                Configure SMTP settings for email delivery
              </p>
            </div>
          </div>

          <div className="space-y-4">
            <div>
              <label className="block text-[13px] text-[#86868B] mb-2" style={{ fontWeight: 500 }}>
                SMTP Host
              </label>
              <input
                type="text"
                defaultValue="smtp.university.edu"
                className="w-full px-4 py-2.5 bg-[#F5F5F7] border-2 border-transparent rounded-xl text-[14px] text-[#1D1D1F] focus:outline-none focus:border-[#0071E3] focus:bg-white transition-all"
              />
            </div>
            <div>
              <label className="block text-[13px] text-[#86868B] mb-2" style={{ fontWeight: 500 }}>
                SMTP Port
              </label>
              <input
                type="text"
                defaultValue="587"
                className="w-full px-4 py-2.5 bg-[#F5F5F7] border-2 border-transparent rounded-xl text-[14px] text-[#1D1D1F] focus:outline-none focus:border-[#0071E3] focus:bg-white transition-all"
              />
            </div>
          </div>
        </div>

        {/* General */}
        <div className="bg-white rounded-2xl border border-[#E5E5EA] p-6">
          <div className="flex items-center gap-3 mb-5">
            <div className="w-9 h-9 bg-cyan-500 rounded-lg flex items-center justify-center">
              <Globe className="w-4 h-4 text-white" strokeWidth={2} />
            </div>
            <div>
              <h2 className="text-[17px] text-[#1D1D1F]" style={{ fontWeight: 600 }}>
                General
              </h2>
              <p className="text-[13px] text-[#86868B]">
                Basic system configuration
              </p>
            </div>
          </div>

          <div className="space-y-4">
            <div>
              <label className="block text-[13px] text-[#86868B] mb-2" style={{ fontWeight: 500 }}>
                System Name
              </label>
              <input
                type="text"
                defaultValue="TA Recruitment System"
                className="w-full px-4 py-2.5 bg-[#F5F5F7] border-2 border-transparent rounded-xl text-[14px] text-[#1D1D1F] focus:outline-none focus:border-[#0071E3] focus:bg-white transition-all"
              />
            </div>
            <div>
              <label className="block text-[13px] text-[#86868B] mb-2" style={{ fontWeight: 500 }}>
                Time Zone
              </label>
              <select className="w-full px-4 py-2.5 bg-[#F5F5F7] border-2 border-transparent rounded-xl text-[14px] text-[#1D1D1F] focus:outline-none focus:border-[#0071E3] focus:bg-white transition-all">
                <option>UTC-5 (Eastern Time)</option>
                <option>UTC-6 (Central Time)</option>
                <option>UTC-7 (Mountain Time)</option>
                <option>UTC-8 (Pacific Time)</option>
              </select>
            </div>
          </div>
        </div>

        {/* Save Button */}
        <div className="flex justify-end">
          <button className="flex items-center gap-2 px-6 py-3 bg-[#0071E3] text-white rounded-xl hover:bg-[#0077ED] transition-all shadow-sm" style={{ fontWeight: 500 }}>
            <Save className="w-4 h-4" strokeWidth={2} />
            <span className="text-[14px]">Save Changes</span>
          </button>
        </div>
      </div>
    </div>
  );
}
