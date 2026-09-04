import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import {
  ShieldCheck,
  Users,
  Briefcase,
  FileText,
  Activity,
  HardDrive,
  BarChart3,
  Lock,
  Search,
  CheckCircle2,
  AlertTriangle,
  UserCheck,
  UserX,
  Trash2,
  Edit3,
  X,
  Sparkles,
  Clock,
} from 'lucide-react';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  Legend,
} from 'recharts';
import { AdminStats, UserItem, RecruiterItem, ActivityLogItem } from '../types/admin';
import { adminService } from '../services/adminService';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

const ROLE_COLORS = ['#0284c7', '#8b5cf6', '#10b981'];

export const AdminDashboardPage: React.FC = () => {
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [users, setUsers] = useState<UserItem[]>([]);
  const [recruiters, setRecruiters] = useState<RecruiterItem[]>([]);
  const [logs, setLogs] = useState<ActivityLogItem[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [activeTab, setActiveTab] = useState<'users' | 'recruiters' | 'analytics' | 'logs'>('users');
  const [searchQuery, setSearchQuery] = useState<string>('');

  // Role Edit Modal
  const [editRoleUser, setEditRoleUser] = useState<UserItem | null>(null);
  const [selectedRole, setSelectedRole] = useState<string>('ROLE_USER');

  const fetchAdminData = async () => {
    setIsLoading(true);
    try {
      const [statsData, usersData, recruitersData, logsData] = await Promise.all([
        adminService.getAdminDashboard(),
        adminService.getAdminUsers(),
        adminService.getAdminRecruiters(),
        adminService.getAdminLogs(),
      ]);
      setStats(statsData);
      setUsers(usersData);
      setRecruiters(recruitersData);
      setLogs(logsData);
    } catch {
      // Fallback
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchAdminData();
  }, []);

  const handleToggleStatus = async (user: UserItem) => {
    const nextStatus = user.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    try {
      await adminService.updateUserStatus(user.id, nextStatus);
      fetchAdminData();
    } catch {
      alert('Failed to update user status.');
    }
  };

  const handleRoleSubmit = async () => {
    if (!editRoleUser) return;
    try {
      await adminService.changeUserRole(editRoleUser.id, selectedRole);
      setEditRoleUser(null);
      fetchAdminData();
    } catch {
      alert('Failed to change user role.');
    }
  };

  const handleDeleteUser = async (userId: number) => {
    if (!window.confirm('Delete this user account permanently?')) return;
    try {
      await adminService.deleteUser(userId);
      fetchAdminData();
    } catch {
      alert('Failed to delete user.');
    }
  };

  const filteredUsers = users.filter(
    (u) =>
      u.fullName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.email.toLowerCase().includes(searchQuery.toLowerCase())
  );

  if (isLoading) {
    return (
      <div className="py-20 flex justify-center">
        <LoadingSpinner size="lg" label="Loading Enterprise Admin & Recruiter Portal..." />
      </div>
    );
  }

  const rolePieData = [
    { name: 'Users', value: users.filter((u) => u.role === 'ROLE_USER').length || 1 },
    { name: 'Recruiters', value: users.filter((u) => u.role === 'ROLE_RECRUITER').length || 1 },
    { name: 'Admins', value: users.filter((u) => u.role === 'ROLE_ADMIN').length || 1 },
  ];

  const userGrowthData = [
    { month: 'Jan', count: 12 },
    { month: 'Feb', count: 25 },
    { month: 'Mar', count: 48 },
    { month: 'Apr', count: 76 },
    { month: 'May', count: 110 },
  ];

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-7xl mx-auto space-y-8 pb-16"
    >
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 dark:text-white flex items-center space-x-3">
            <ShieldCheck className="w-8 h-8 text-primary-500" />
            <span>Enterprise Admin Portal</span>
          </h1>
          <p className="text-xs sm:text-sm text-gray-500 dark:text-gray-400 mt-1">
            Role-Based Access Control (RBAC), User/Recruiter Management, Audit Logs, and System Metrics.
          </p>
        </div>
      </div>

      {/* Stats Cards Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
        <Card className="p-4 space-y-1">
          <span className="text-[10px] font-bold uppercase text-gray-400">Total Users</span>
          <span className="text-2xl font-extrabold text-gray-900 dark:text-white block">{stats?.totalUsers || 0}</span>
        </Card>

        <Card className="p-4 space-y-1">
          <span className="text-[10px] font-bold uppercase text-gray-400">Recruiters</span>
          <span className="text-2xl font-extrabold text-purple-600 dark:text-purple-400 block">{stats?.totalRecruiters || 0}</span>
        </Card>

        <Card className="p-4 space-y-1">
          <span className="text-[10px] font-bold uppercase text-gray-400">Resumes</span>
          <span className="text-2xl font-extrabold text-indigo-600 dark:text-indigo-400 block">{stats?.totalResumes || 0}</span>
        </Card>

        <Card className="p-4 space-y-1">
          <span className="text-[10px] font-bold uppercase text-gray-400">Reports</span>
          <span className="text-2xl font-extrabold text-emerald-600 dark:text-emerald-400 block">{stats?.totalReports || 0}</span>
        </Card>

        <Card className="p-4 space-y-1">
          <span className="text-[10px] font-bold uppercase text-gray-400">Avg ATS</span>
          <span className="text-2xl font-extrabold text-amber-600 dark:text-amber-400 block">{stats?.averageAtsScore || 0}</span>
        </Card>

        <Card className="p-4 space-y-1">
          <span className="text-[10px] font-bold uppercase text-gray-400">Storage Used</span>
          <span className="text-lg font-bold text-gray-700 dark:text-gray-300 block">{stats?.storageUsed || '48 MB'}</span>
        </Card>
      </div>

      {/* Tabbed Navigation */}
      <Card className="p-6 space-y-6">
        <div className="flex border-b border-gray-200 dark:border-dark-border space-x-6">
          {[
            { id: 'users', label: 'User Management', icon: Users },
            { id: 'recruiters', label: 'Recruiter Management', icon: Briefcase },
            { id: 'analytics', label: 'System Analytics', icon: BarChart3 },
            { id: 'logs', label: 'Audit Logs', icon: Clock },
          ].map((tab) => {
            const Icon = tab.icon;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as any)}
                className={`pb-3 text-xs sm:text-sm font-semibold transition-colors flex items-center space-x-2 ${
                  activeTab === tab.id
                    ? 'border-b-2 border-primary-600 text-primary-600 dark:text-primary-400'
                    : 'text-gray-500 hover:text-gray-700 dark:text-gray-400'
                }`}
              >
                <Icon className="w-4 h-4" />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </div>

        {/* Tab 1: User Management Table */}
        {activeTab === 'users' && (
          <div className="space-y-4">
            <div className="relative max-w-sm">
              <Search className="w-4 h-4 text-gray-400 absolute left-3 top-3" />
              <input
                type="text"
                placeholder="Search user name or email..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-9 pr-4 py-2 bg-gray-50 dark:bg-dark-hover border border-gray-200 dark:border-dark-border rounded-xl text-xs sm:text-sm"
              />
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs sm:text-sm">
                <thead>
                  <tr className="border-b border-gray-200 dark:border-dark-border text-gray-400 font-bold uppercase">
                    <th className="py-3 px-4">User</th>
                    <th className="py-3 px-4">Role</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4">Joined Date</th>
                    <th className="py-3 px-4">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredUsers.map((u) => (
                    <tr key={u.id} className="border-b border-gray-100 dark:border-dark-border/50">
                      <td className="py-3 px-4">
                        <span className="font-bold text-gray-900 dark:text-white block">{u.fullName}</span>
                        <span className="text-gray-400 text-xs">{u.email}</span>
                      </td>
                      <td className="py-3 px-4">
                        <Badge
                          variant={u.role === 'ROLE_ADMIN' ? 'purple' : u.role === 'ROLE_RECRUITER' ? 'warning' : 'primary'}
                          size="sm"
                        >
                          {u.role}
                        </Badge>
                      </td>
                      <td className="py-3 px-4">
                        <Badge variant={u.status === 'ACTIVE' ? 'success' : 'purple'} size="sm">
                          {u.status}
                        </Badge>
                      </td>
                      <td className="py-3 px-4 text-gray-500">
                        {new Date(u.joinedDate).toLocaleDateString()}
                      </td>
                      <td className="py-3 px-4 flex items-center space-x-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => {
                            setEditRoleUser(u);
                            setSelectedRole(u.role);
                          }}
                          leftIcon={<Edit3 className="w-3.5 h-3.5" />}
                        >
                          Edit Role
                        </Button>

                        <Button
                          variant={u.status === 'ACTIVE' ? 'outline' : 'primary'}
                          size="sm"
                          onClick={() => handleToggleStatus(u)}
                        >
                          {u.status === 'ACTIVE' ? 'Suspend' : 'Activate'}
                        </Button>

                        <Button
                          variant="danger"
                          size="sm"
                          onClick={() => handleDeleteUser(u.id)}
                          leftIcon={<Trash2 className="w-3.5 h-3.5" />}
                        >
                          Delete
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Tab 2: Recruiter Management */}
        {activeTab === 'recruiters' && (
          <div className="space-y-4">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs sm:text-sm">
                <thead>
                  <tr className="border-b border-gray-200 dark:border-dark-border text-gray-400 font-bold uppercase">
                    <th className="py-3 px-4">Company</th>
                    <th className="py-3 px-4">Recruiter Email</th>
                    <th className="py-3 px-4">Industry</th>
                    <th className="py-3 px-4">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {recruiters.map((rec) => (
                    <tr key={rec.id} className="border-b border-gray-100 dark:border-dark-border/50">
                      <td className="py-3 px-4 font-bold text-gray-900 dark:text-white">{rec.companyName}</td>
                      <td className="py-3 px-4 text-gray-500">{rec.companyEmail}</td>
                      <td className="py-3 px-4 text-gray-500">{rec.industry}</td>
                      <td className="py-3 px-4">
                        <Badge variant={rec.status === 'ACTIVE' ? 'success' : 'purple'} size="sm">
                          {rec.status}
                        </Badge>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Tab 3: System Analytics */}
        {activeTab === 'analytics' && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <Card className="space-y-4">
              <h3 className="text-base font-bold text-gray-900 dark:text-white">User Growth Growth Timeline</h3>
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={userGrowthData}>
                    <XAxis dataKey="month" stroke="#888888" fontSize={11} />
                    <YAxis stroke="#888888" fontSize={11} />
                    <Tooltip />
                    <Line type="monotone" dataKey="count" stroke="#0284c7" strokeWidth={3} />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </Card>

            <Card className="space-y-4">
              <h3 className="text-base font-bold text-gray-900 dark:text-white">User Role Allocation</h3>
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie data={rolePieData} cx="50%" cy="50%" innerRadius={60} outerRadius={80} dataKey="value">
                      {rolePieData.map((_, index) => (
                        <Cell key={`cell-${index}`} fill={ROLE_COLORS[index % ROLE_COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            </Card>
          </div>
        )}

        {/* Tab 4: Audit Logs Table */}
        {activeTab === 'logs' && (
          <div className="space-y-4">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs sm:text-sm">
                <thead>
                  <tr className="border-b border-gray-200 dark:border-dark-border text-gray-400 font-bold uppercase">
                    <th className="py-3 px-4">Action</th>
                    <th className="py-3 px-4">Description</th>
                    <th className="py-3 px-4">User</th>
                    <th className="py-3 px-4">Timestamp</th>
                  </tr>
                </thead>
                <tbody>
                  {logs.map((log) => (
                    <tr key={log.id} className="border-b border-gray-100 dark:border-dark-border/50">
                      <td className="py-3 px-4 font-bold text-primary-600 dark:text-primary-400">{log.action}</td>
                      <td className="py-3 px-4 text-gray-700 dark:text-gray-300">{log.description}</td>
                      <td className="py-3 px-4 text-gray-500">{log.userEmail}</td>
                      <td className="py-3 px-4 text-gray-400 text-xs">{new Date(log.timestamp).toLocaleString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </Card>

      {/* Edit Role Modal */}
      {editRoleUser && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-gray-900/50 backdrop-blur-sm">
          <Card className="w-full max-w-md p-6 space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-bold text-gray-900 dark:text-white">Assign User Role</h3>
              <button onClick={() => setEditRoleUser(null)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <p className="text-xs text-gray-500">
              Select new role authority for <span className="font-bold text-gray-900 dark:text-white">{editRoleUser.email}</span>
            </p>

            <select
              value={selectedRole}
              onChange={(e) => setSelectedRole(e.target.value)}
              className="w-full px-4 py-2 bg-gray-50 dark:bg-dark-hover border border-gray-200 dark:border-dark-border rounded-xl text-sm"
            >
              <option value="ROLE_USER">ROLE_USER (Standard Candidate User)</option>
              <option value="ROLE_RECRUITER">ROLE_RECRUITER (HR Recruiter Access)</option>
              <option value="ROLE_ADMIN">ROLE_ADMIN (Full Platform Administrator)</option>
            </select>

            <div className="flex justify-end space-x-3 pt-2">
              <Button variant="outline" size="sm" onClick={() => setEditRoleUser(null)}>
                Cancel
              </Button>
              <Button variant="primary" size="sm" onClick={handleRoleSubmit}>
                Save Role Change
              </Button>
            </div>
          </Card>
        </div>
      )}
    </motion.div>
  );
};
