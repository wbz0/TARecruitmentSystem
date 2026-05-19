import { useState } from "react";
import { Search, MoreVertical, UserCheck, UserX, Shield } from "lucide-react";

interface User {
  id: string;
  name: string;
  email: string;
  role: "TA" | "MO" | "Admin";
  status: "Active" | "Inactive";
  joinDate: string;
  applications?: number;
  hires?: number;
}

export function AdminUsers() {
  const [searchQuery, setSearchQuery] = useState("");

  const users: User[] = [
    { id: "1", name: "Sarah Jenkins", email: "sarah.j@university.edu", role: "TA", status: "Active", joinDate: "2024-09-15", applications: 5 },
    { id: "2", name: "Michael Scott", email: "michael.s@university.edu", role: "MO", status: "Active", joinDate: "2024-08-20", hires: 12 },
    { id: "3", name: "John Doe", email: "john.d@university.edu", role: "TA", status: "Active", joinDate: "2024-10-01", applications: 3 },
    { id: "4", name: "Jane Smith", email: "jane.s@university.edu", role: "MO", status: "Active", joinDate: "2024-07-10", hires: 8 },
    { id: "5", name: "Robert Brown", email: "robert.b@university.edu", role: "TA", status: "Inactive", joinDate: "2024-06-05", applications: 1 },
    { id: "6", name: "Admin User", email: "admin@university.edu", role: "Admin", status: "Active", joinDate: "2024-01-01" },
  ];

  const filteredUsers = users.filter(user => 
    user.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    user.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
    user.role.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const getRoleBadgeStyle = (role: string) => {
    switch (role) {
      case "Admin":
        return "bg-purple-50 text-purple-700 border-purple-200";
      case "MO":
        return "bg-orange-50 text-orange-700 border-orange-200";
      case "TA":
        return "bg-blue-50 text-blue-700 border-blue-200";
      default:
        return "bg-gray-50 text-gray-700 border-gray-200";
    }
  };

  const getStatusStyle = (status: string) => {
    return status === "Active" 
      ? "text-green-600" 
      : "text-gray-400";
  };

  return (
    <div className="p-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-[28px] text-[#1D1D1F] mb-2" style={{ fontWeight: 600 }}>
          User Management
        </h1>
        <p className="text-[15px] text-[#86868B]">
          Manage all users across the TA recruitment system
        </p>
      </div>

      {/* Search and filters */}
      <div className="mb-6">
        <div className="relative">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-[#86868B]" strokeWidth={2} />
          <input
            type="text"
            placeholder="Search by name, email or role..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-11 pr-4 py-3 bg-[#F5F5F7] border-2 border-transparent rounded-xl text-[14px] text-[#1D1D1F] placeholder:text-[#86868B] focus:outline-none focus:border-[#0071E3] focus:bg-white transition-all"
            style={{ fontWeight: 400 }}
          />
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-3 gap-4 mb-6">
        <div className="bg-gradient-to-br from-blue-50 to-blue-100/50 rounded-2xl p-5 border border-blue-200/50">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-8 h-8 bg-blue-500 rounded-lg flex items-center justify-center">
              <UserCheck className="w-4 h-4 text-white" strokeWidth={2} />
            </div>
            <span className="text-[13px] text-blue-700" style={{ fontWeight: 500 }}>Total TAs</span>
          </div>
          <div className="text-[28px] text-blue-900" style={{ fontWeight: 600 }}>
            {users.filter(u => u.role === "TA").length}
          </div>
        </div>

        <div className="bg-gradient-to-br from-orange-50 to-orange-100/50 rounded-2xl p-5 border border-orange-200/50">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-8 h-8 bg-orange-500 rounded-lg flex items-center justify-center">
              <UserCheck className="w-4 h-4 text-white" strokeWidth={2} />
            </div>
            <span className="text-[13px] text-orange-700" style={{ fontWeight: 500 }}>Total MOs</span>
          </div>
          <div className="text-[28px] text-orange-900" style={{ fontWeight: 600 }}>
            {users.filter(u => u.role === "MO").length}
          </div>
        </div>

        <div className="bg-gradient-to-br from-purple-50 to-purple-100/50 rounded-2xl p-5 border border-purple-200/50">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-8 h-8 bg-purple-500 rounded-lg flex items-center justify-center">
              <Shield className="w-4 h-4 text-white" strokeWidth={2} />
            </div>
            <span className="text-[13px] text-purple-700" style={{ fontWeight: 500 }}>Total Admins</span>
          </div>
          <div className="text-[28px] text-purple-900" style={{ fontWeight: 600 }}>
            {users.filter(u => u.role === "Admin").length}
          </div>
        </div>
      </div>

      {/* Users Table */}
      <div className="bg-white rounded-2xl border border-[#E5E5EA] overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-[#F5F5F7] border-b border-[#E5E5EA]">
                <th className="px-6 py-4 text-left text-[13px] text-[#86868B]" style={{ fontWeight: 500 }}>
                  User
                </th>
                <th className="px-6 py-4 text-left text-[13px] text-[#86868B]" style={{ fontWeight: 500 }}>
                  Role
                </th>
                <th className="px-6 py-4 text-left text-[13px] text-[#86868B]" style={{ fontWeight: 500 }}>
                  Status
                </th>
                <th className="px-6 py-4 text-left text-[13px] text-[#86868B]" style={{ fontWeight: 500 }}>
                  Join Date
                </th>
                <th className="px-6 py-4 text-left text-[13px] text-[#86868B]" style={{ fontWeight: 500 }}>
                  Activity
                </th>
                <th className="px-6 py-4 text-right text-[13px] text-[#86868B]" style={{ fontWeight: 500 }}>
                  Actions
                </th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.map((user) => (
                <tr key={user.id} className="border-b border-[#E5E5EA] hover:bg-[#F5F5F7]/50 transition-colors">
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <div className={`w-9 h-9 rounded-full flex items-center justify-center text-white text-[14px] ${
                        user.role === "Admin" ? "bg-gradient-to-br from-purple-400 to-purple-600" :
                        user.role === "MO" ? "bg-gradient-to-br from-orange-400 to-orange-600" :
                        "bg-gradient-to-br from-blue-400 to-blue-600"
                      }`} style={{ fontWeight: 600 }}>
                        {user.name.split(' ').map(n => n[0]).join('')}
                      </div>
                      <div>
                        <div className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 500 }}>
                          {user.name}
                        </div>
                        <div className="text-[13px] text-[#86868B]">
                          {user.email}
                        </div>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex items-center px-3 py-1 rounded-full text-[12px] border ${getRoleBadgeStyle(user.role)}`} style={{ fontWeight: 500 }}>
                      {user.role}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      <div className={`w-2 h-2 rounded-full ${user.status === "Active" ? "bg-green-500" : "bg-gray-300"}`} />
                      <span className={`text-[14px] ${getStatusStyle(user.status)}`} style={{ fontWeight: 500 }}>
                        {user.status}
                      </span>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <span className="text-[14px] text-[#1D1D1F]">
                      {new Date(user.joinDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <span className="text-[14px] text-[#1D1D1F]">
                      {user.applications !== undefined && `${user.applications} applications`}
                      {user.hires !== undefined && `${user.hires} hires`}
                      {user.applications === undefined && user.hires === undefined && "—"}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex justify-end">
                      <button className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-[#F5F5F7] transition-colors">
                        <MoreVertical className="w-4 h-4 text-[#86868B]" strokeWidth={2} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {filteredUsers.length === 0 && (
        <div className="text-center py-12">
          <UserX className="w-12 h-12 text-[#86868B] mx-auto mb-3" strokeWidth={1.5} />
          <p className="text-[15px] text-[#86868B]">No users found</p>
        </div>
      )}
    </div>
  );
}
