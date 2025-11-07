@echo off

SET TEMPLATE_FILE=main.yml
SET PARAMS_FILE=./params.json

SET /P DB_IDENTIFIER=Ingrese el identificador de la BD (por defecto bookstore-db-lambdas):
IF "%DB_IDENTIFIER%"=="" SET DB_IDENTIFIER=bookstore-db-lambdas

SET /P DB_TYPE=Ingrese el tipo de BD (por defecto postgresql):
IF "%DB_TYPE%"=="" SET DB_TYPE=postgresql

SET /P DB_NAME=Ingrese el nombre de la BD (por defecto bookstore-lambdas):
IF "%DB_NAME%"=="" SET DB_NAME=bookstore

SET /P DB_USER=Ingrese el usuario de la BD (por defecto postgres):
IF "%DB_USER%"=="" SET DB_USER=postgres

FOR /F "usebackq tokens=*" %%A IN (`powershell -Command "[Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR((Read-Host -AsSecureString 'Ingrese la contraseña de la base de datos')))"`) DO SET DB_PASSWORD=%%A

FOR /F "tokens=*" %%A IN ('aws ec2 describe-vpcs --query "Vpcs[0].VpcId" --output text') DO SET VPC_ID=%%A
echo VPC ID: %VPC_ID%

FOR /F "tokens=*" %%A IN ('aws ec2 describe-subnets --filters "Name=vpc-id,Values=%VPC_ID%" --query "join(',', Subnets[].SubnetId)" --output text') DO SET "SUBNETS=%%A"
echo Subnets: %SUBNETS%

IF "%VPC_ID%"=="" (
    echo ERROR: No se pudo obtener el VPC ID
    pause
    exit /b 1
)

IF "%SUBNETS%"=="" (
    echo ERROR: No se pudieron obtener las subnets
    pause
    exit /b 1
)

echo Generando archivo %PARAMS_FILE%...

REM Crear archivo JSON con los parámetros
(
echo [
echo   {"ParameterKey": "SubnetIds", "ParameterValue": "%SUBNETS%"},
echo   {"ParameterKey": "VpcId", "ParameterValue": "%VPC_ID%"},
echo   {"ParameterKey": "DBIdentifier", "ParameterValue": "%DB_IDENTIFIER%"},
echo   {"ParameterKey": "DBType", "ParameterValue": "%DB_TYPE%"},
echo   {"ParameterKey": "DBName", "ParameterValue": "%DB_NAME%"},
echo   {"ParameterKey": "DBUser", "ParameterValue": "%DB_USER%"},
echo   {"ParameterKey": "DBPassword", "ParameterValue": "%DB_PASSWORD%"}
echo ]
) > %PARAMS_FILE%

echo Archivo %PARAMS_FILE% generado exitosamente.
pause
