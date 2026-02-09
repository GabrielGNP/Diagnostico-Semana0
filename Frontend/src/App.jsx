import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import "./App.css";
import Dashboard from "./components/Dashboard";
import AddUser from "./components/AddUser";
import AddOrder from "./components/AddOrder";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/addUser" element={<AddUser />} />
        <Route path="/addOrder" element={<AddOrder />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
