import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import "./App.css";
import Dashboard from "./components/Dashboard";
import AddUser from "./components/AddUser";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/addUser" element={<AddUser />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
