import React, { useState, useEffect, useCallback } from "react";
import UsersService from "../services/UsersService";
import { Link, useNavigate } from "react-router-dom";
import "./ProfilePage.css";

function ProfilePage() {
    const [profileInfo, setProfileInfo] = useState({
        name: '',
        email: '',
        city: '',
        role: '',
        id: ''
    });
    const [isLoading, setIsLoading] = useState(true);
    const navigate = useNavigate();

    const fetchProfileInfo = useCallback(async () => {
        try {
            const token = localStorage.getItem('token');
            if (!token) throw new Error('No token found');

            const response = await UsersService.getYourProfile(token);
            const userData = response.ourUsers || {};
            setProfileInfo(userData);
            
            if (!localStorage.getItem('userId') && userData.id) {
                localStorage.setItem('userId', userData.id);
            }
        } catch (error) {
            console.error('Profile fetch error:', error);
            navigate('/login');
        } finally {
            setIsLoading(false);
        }
    }, [navigate]);

    useEffect(() => {
        fetchProfileInfo();
    }, [fetchProfileInfo]);

    const canEditProfile = () => {
        const currentUserId = localStorage.getItem('userId');
        return currentUserId && currentUserId === profileInfo.id?.toString();
    };

    if (isLoading) {
        return <div className="loading-message">Chargement du profil...</div>;
    }

    return (
        <div className="profile-page-container">
            {/* Message de bienvenue et statut */}
            <div className="welcome-banner">
                <h2>Bienvenue, {profileInfo.name || 'cher utilisateur'} !</h2>
                <div className="connection-status">
                    <span className="status-dot connected"></span>
                    <span>Vous êtes connecté en tant que {profileInfo.role || 'utilisateur'}</span>
                </div>
            </div>

            {/* Informations du profil */}
            <div className="profile-section">
                <h3>Vos informations personnelles</h3>
                <div className="profile-details">
                    <p><strong>Email :</strong> {profileInfo.email || 'Non renseigné'}</p>
                    <p><strong>Ville :</strong> {profileInfo.city || 'Non renseignée'}</p>
                </div>
            </div>

            {/* Bouton d'édition avec message contextuel */}
            {(canEditProfile() || profileInfo.role === "ADMIN") && (
                <div className="edit-section">
                    <p className="edit-info">
                        {canEditProfile() 
                            ? "Vous pouvez modifier vos informations personnelles ci-dessous :"
                            : "En tant qu'administrateur, vous pouvez modifier ce profil :"}
                    </p>
                    <button className="update-profile-btn">
                        <Link to={`/update-user/${profileInfo.id}`}>
                            {profileInfo.role === "ADMIN" 
                                ? "Modifier ce profil" 
                                : "Modifier mon profil"}
                        </Link>
                    </button>
                </div>
            )}
        </div>
    );
}

export default ProfilePage;