import React from "react";
import "./Footer.css"; // Import du CSS

const FooterComponent = () => {
  return (
    <footer className="footer">
      <div className="footer-content">
        <div className="footer-section">
          <h3>Dansunsation Dev</h3>
          <p>Développement web sur mesure</p>
          <p>Solutions innovantes pour vos projets</p>
        </div>

        <div className="footer-section">
          <h3>Contact</h3>
          <a href="mailto:contact@dansunsation.dev">contact@dansunsation.dev</a>
          <div className="social-links">
            <a href="https://github.com" aria-label="GitHub">GitHub</a>
            <a href="https://linkedin.com" aria-label="LinkedIn">LinkedIn</a>
            <a href="https://twitter.com" aria-label="Twitter">Twitter</a>
          </div>
        </div>
      </div>

      <div className="footer-bottom">
        <p>&copy; {new Date().getFullYear()} Dansunsation Dev - Tous droits réservés</p>
      </div>
    </footer>
  );
};

export default FooterComponent;