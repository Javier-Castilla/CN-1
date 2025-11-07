@echo off

SET /P STACK_NAME=Ingrese el nombre de la pila:
SET TEMPLATE_FILE=ECSStructure/main.yml
SET VPC_NAME=MiVPC

SET IMAGE_NAME=acoplado:latest
SET DB_IDENTIFIER=bookstore-db
SET DB_TYPE=postgresql
SET DB_NAME=bookstore
SET DB_USER=postgres
SET /P DB_PASSWORD=Ingrese la contraseña de la base de datos:

echo.
echo Obteniendo VPC por defecto...
FOR /F "tokens=*" %%A IN ('aws ec2 describe-vpcs --query "Vpcs[0].VpcId" --output text') DO SET VPC_ID=%%A
echo VPC ID: %VPC_ID%

echo Obteniendo subnets...
FOR /F "tokens=*" %%A IN ('aws ec2 describe-subnets --filters "Name=vpc-id,Values=%VPC_ID%" --query "Subnets[*].SubnetId" --output text') DO SET SUBNETS=%%A
SET SUBNETS=%SUBNETS: =,%
echo Subnets: %SUBNETS%

echo Obteniendo Route Table...
FOR /F "tokens=*" %%A IN ('aws ec2 describe-route-tables --filters "Name=vpc-id,Values=%VPC_ID%" --query "RouteTables[0].RouteTableId" --output text') DO SET ROUTE_TABLE_ID=%%A
echo Route Table ID: %ROUTE_TABLE_ID%

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

IF "%ROUTE_TABLE_ID%"=="" (
    echo ERROR: No se pudo obtener la Route Table
    pause
    exit /b 1
)

echo Obteniendo Account ID...
FOR /F "tokens=*" %%A IN ('aws sts get-caller-identity --query "Account" --output text') DO SET ACCOUNT_ID=%%A
SET ROLE_ARN=arn:aws:iam::%ACCOUNT_ID%:role/LabRole
echo Usando rol: %ROLE_ARN%

echo.
echo Creando stack de CloudFormation...
echo Stack Name: %STACK_NAME%
echo Template: %TEMPLATE_FILE%
echo.

aws cloudformation create-stack ^
    --stack-name %STACK_NAME% ^
    --template-body file://%TEMPLATE_FILE% ^
    --role-arn arn:aws:iam::<tu_account_id>:role/LabRole ^
    --parameters ParameterKey=ImageName,ParameterValue=%IMAGE_NAME% ^
                 ParameterKey=SubnetIds,ParameterValue="%SUBNETS%" ^
                 ParameterKey=RouteTableId,ParameterValue=%ROUTE_TABLE_ID% ^
                 ParameterKey=VpcId,ParameterValue=%VPC_ID% ^
                 ParameterKey=DBIdentifier,ParameterValue=%DB_IDENTIFIER% ^
                 ParameterKey=DBType,ParameterValue=%DB_TYPE% ^
                 ParameterKey=DBName,ParameterValue=%DB_NAME% ^
                 ParameterKey=DBUser,ParameterValue=%DB_USER% ^
                 ParameterKey=DBPassword,ParameterValue=%DB_PASSWORD% ^
    --capabilities CAPABILITY_NAMED_IAM

IF %ERRORLEVEL% EQU 0 (
    echo.
    echo Stack creado exitosamente!
    echo Puedes monitorear el progreso con:
    echo aws cloudformation describe-stacks --stack-name %STACK_NAME%
) ELSE (
    echo.
    echo ERROR: Fallo la creacion del stack
)

pause