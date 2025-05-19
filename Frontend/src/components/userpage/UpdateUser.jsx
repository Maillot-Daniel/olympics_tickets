import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import UsersService from "../services/UsersService";
import { useNavigate } from "react-router-dom";


function UpdateUser() {
    const navigate = useNavigate();
    const { userId } = useParams();


    const [userData, setUserData] = useState({
        name: '',
        email: '',
        role: '',
        city: ''
    });

    useEffect(() => {
        fetchUserDataById(userId); // Pass the userId to fetchUserDataById
    }, [userId]);

    const fetchUserDataById = async (userId) => {
        try {
            const token = localStorage.getItem('token');
            const response = await UsersService.getUserById(userId, token); // Pass userId to getUserId
            const { name, email, role, city } = response.ourUsers;
            setUserData({ name, email, role, city });
        } catch (error) {
            console.error('Error fetching user data:', error);
        }
    };


    const handleInputChange = (e) => {
    const { name, value } = e.target;
    setUserData((prevUserData) => ({
        ...prevUserData,
        [name]: value
    }));
};

    const handelSubmit = async (e) => {
        e.preventDefault();
        try {
            const confirDelete = window.confirm('Are you sure you want to delete this user?');
            if (confirDelete) {
                const token = localStorage.getItem('token');
                await UsersService.updateUser(userId, userData, token);
                // Redirect to profile page or display a success message
                navigate("/admin/user-management")
            }

        } catch (error) {
            console.error('Error updating user profile', error);
            alert(error)
        }
    };

    return (
        <div className="auth-container">
            <h2>Update User</h2>
            <form onSubmit={handelSubmit}>
                <div className="form-group">
                    <label>Name:</label>
                    <input type="text" name="name" value={userData.name} onChange={handleInputChange} />
                </div>
                <div className="form-group">
                    <label>Email:</label>
                    <input type="email" name="email" value={userData.email} onChange={handleInputChange} />
                </div>
                <div className="form-group">
                    <label>Role:</label>
                    <input type="text" name="role" value={userData.role} onChange={handleInputChange} />
                </div>
                <div className="form-group">
                    <label>City:</label>
                    <input type="text" name="city" value={userData.city} onChange={handleInputChange} 
                    />
                </div>
                <button type="submit">Update</button>
            </form>
        </div>
    );
}

export default UpdateUser;