#!/bin/bash
set -e  # Detener el script si ocurre error
export DOCKER_BUILDKIT=0

# ===============================================
# 🚀 Script para construir y subir una imagen Docker a ECR
# Compatible con macOS / Linux
# ===============================================

# ==== 🔧 CONFIGURACIÓN MODIFICABLE ====
ECR_REPOSITORY="${1:-monolith}"    # ← Cambia esto si quieres subir a otro repo
IMAGE_NAME="monolith"        # ← Nombre local de la imagen (puede ser distinto del repo)
TAG="latest"                 # ← Puedes cambiar a 'v1.0.0', 'dev', etc.
# ======================================

# Ir al directorio del script
cd "$(dirname "$0")"

echo "=============================================="
echo "🚀 Iniciando build y push de imagen Docker"
echo "=============================================="

# Detectar región AWS configurada
REGION=$(aws configure get region)
if [[ -z "$REGION" ]]; then
  echo "⚠️  No se detectó región en configuración de AWS. Usando 'us-east-1' por defecto."
  REGION="us-east-1"
fi

# Obtener ID de cuenta AWS
ACCOUNT_ID=$(aws sts get-caller-identity --query "Account" --output text)
if [[ -z "$ACCOUNT_ID" || "$ACCOUNT_ID" == "None" ]]; then
  echo "❌ ERROR: No se pudo obtener el ID de cuenta AWS. Verifica tus credenciales (aws configure)."
  exit 1
fi

# Construir URL completo de ECR
ECR_URL="${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com"
FULL_IMAGE="${ECR_URL}/${ECR_REPOSITORY}:${TAG}"

echo ""
echo "=============================================="
echo "📋 Configuración detectada:"
echo "Cuenta AWS:  ${ACCOUNT_ID}"
echo "Región:      ${REGION}"
echo "Repositorio: ${ECR_REPOSITORY}"
echo "Imagen:      ${FULL_IMAGE}"
echo "=============================================="
echo ""

# Compilar con Maven si aplica
if [[ -f "./mvnw" ]]; then
  echo "🔧 Compilando proyecto Maven..."
  ./mvnw clean package -DskipTests -U -X
else
  echo "⚠️  No se encontró ./mvnw — se omite compilación Maven."
fi

# Iniciar sesión en Amazon ECR
echo ""
echo "🔐 Iniciando sesión en Amazon ECR..."
aws ecr get-login-password --region "${REGION}" | \
docker login --username AWS --password-stdin "${ECR_URL}"

# Crear repositorio si no existe
echo ""
echo "🪣 Verificando existencia del repositorio '${ECR_REPOSITORY}'..."
aws ecr describe-repositories --repository-names "${ECR_REPOSITORY}" --region "${REGION}" >/dev/null 2>&1 || \
aws ecr create-repository --repository-name "${ECR_REPOSITORY}" --region "${REGION}"

# Construir imagen Docker
echo ""
echo "🏗️  Construyendo imagen Docker..."
docker build -t "${IMAGE_NAME}" .

# Etiquetar imagen
echo ""
echo "🏷️  Etiquetando imagen..."
docker tag "${IMAGE_NAME}:${TAG}" "${FULL_IMAGE}"

# Subir imagen
echo ""
echo "☁️  Subiendo imagen a ECR..."
docker push "${FULL_IMAGE}"

echo ""
echo "✅ Imagen publicada correctamente:"
echo "   ${FULL_IMAGE}"
