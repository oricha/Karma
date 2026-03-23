import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import { HelmetProvider } from "react-helmet-async";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import Navbar from "@/components/layout/Navbar";
import Footer from "@/components/layout/Footer";
import HomePage from "./pages/HomePage";
import EventListPage from "./pages/EventListPage";
import EventDetailPage from "./pages/EventDetailPage";
import GroupListPage from "./pages/GroupListPage";
import GroupDetailPage from "./pages/GroupDetailPage";
import LoginPage from "./pages/auth/LoginPage";
import RegisterPage from "./pages/auth/RegisterPage";
import ForgotPasswordPage from "./pages/auth/ForgotPasswordPage";
import ResetPasswordPage from "./pages/auth/ResetPasswordPage";
import VerifyEmailPage from "./pages/auth/VerifyEmailPage";
import MyGroupsPage from "./pages/account/MyGroupsPage";
import MyEventsPage from "./pages/account/MyEventsPage";
import OrdersPage from "./pages/account/OrdersPage";
import AccountDetailsPage from "./pages/account/AccountDetailsPage";
import PreferencesPage from "./pages/account/PreferencesPage";
import SavedEventsPage from "./pages/account/SavedEventsPage";
import NotFound from "./pages/NotFound";
import ProtectedRoute from "./components/auth/ProtectedRoute";

const queryClient = new QueryClient();

const App = () => (
  <HelmetProvider>
    <QueryClientProvider client={queryClient}>
      <TooltipProvider>
        <Toaster />
        <Sonner />
        <BrowserRouter>
          <div className="flex flex-col min-h-screen">
            <Navbar />
            <main className="flex-1">
              <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="/events" element={<EventListPage />} />
                <Route path="/events/:slug" element={<EventDetailPage />} />
                <Route path="/events/category/:slug" element={<EventListPage />} />
                <Route path="/groups" element={<GroupListPage />} />
                <Route path="/groups/:slug" element={<GroupDetailPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                <Route path="/reset-password" element={<ResetPasswordPage />} />
                <Route path="/verify-email" element={<VerifyEmailPage />} />
                <Route element={<ProtectedRoute />}>
                  <Route path="/account/groups" element={<MyGroupsPage />} />
                  <Route path="/account/events" element={<MyEventsPage />} />
                  <Route path="/account/orders" element={<OrdersPage />} />
                  <Route path="/account/details" element={<AccountDetailsPage />} />
                  <Route path="/account/preferences" element={<PreferencesPage />} />
                  <Route path="/account/saved-events" element={<SavedEventsPage />} />
                </Route>
                <Route path="*" element={<NotFound />} />
              </Routes>
            </main>
            <Footer />
          </div>
        </BrowserRouter>
      </TooltipProvider>
    </QueryClientProvider>
  </HelmetProvider>
);

export default App;
