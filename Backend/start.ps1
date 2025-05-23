# Charger les variables d'environnement du fichier .env
Get-Content .env | ForEach-Object {
    if ($_ -match "^\s*([^#][^=]+)=(.+)$") {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        [System.Environment]::SetEnvironmentVariable($name, $value, "Process")
    }
}

# Lancer l'application Spring Boot
mvn spring-boot:run