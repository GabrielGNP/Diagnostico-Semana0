import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import "./App.css";
import Dashboard from "./components/Dashboard";
import AddUser from "./components/AddUser";
import AddOrder from "./components/AddOrder";
import OrderDetail from "./components/OrderDetail";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/addUser" element={<AddUser />} />
        <Route path="/addOrder" element={<AddOrder />} />
        <Route path="/order/:id" element={<OrderDetail />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
