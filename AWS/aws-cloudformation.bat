@echo off
SET STACK_NAME=MiStack
SET TEMPLATE_FILE=stack.yml
SET VPC_NAME=MiVPC   REM Cambia esto al nombre de tu VPC

SET IMAGE_NAME=acoplado:latest
SET DB_IDENTIFIER=bookstore-db
SET DB_TYPE=postgresql
SET DB_NAME=bookstore
SET DB_USER=postgres
SET /P DB_PASSWORD=Ingrese la contraseña de la base de datos:

REM Obtener el VPC automáticamente por nombre
FOR /F "tokens=*" %%A IN ('aws ec2 describe-vpcs --filters "Name=tag:Name,Values=%VPC_NAME%" --query "Vpcs[0].VpcId" --output text') DO SET VPC_ID=%%A

REM Si no se encontró por nombre, usar la primera VPC disponible
IF "%VPC_ID%"=="" (
    FOR /F "tokens=*" %%A IN ('aws ec2 describe-vpcs --query "Vpcs[0].VpcId" --output text') DO SET VPC_ID=%%A
)

REM Obtener todas las subnets del VPC
FOR /F "tokens=*" %%A IN ('aws ec2 describe-subnets --filters "Name=vpc-id,Values=%VPC_ID%" --query "Subnets[*].SubnetId" --output text') DO SET SUBNETS=%%A
SET SUBNETS=%SUBNETS: =,%

aws cloudformation create-stack ^
    --stack-name %STACK_NAME% ^
    --template-body file://%TEMPLATE_FILE% ^
    --parameters ParameterKey=ImageName,ParameterValue=%IMAGE_NAME% ^
                 ParameterKey=SubnetIds,ParameterValue=%SUBNETS% ^
                 ParameterKey=VpcId,ParameterValue=%VPC_ID% ^
                 ParameterKey=DBIdentifier,ParameterValue=%DB_IDENTIFIER% ^
                 ParameterKey=DBType,ParameterValue=%DB_TYPE% ^
                 ParameterKey=DBName,ParameterValue=%DB_NAME% ^
                 ParameterKey=DBUser,ParameterValue=%DB_USER% ^
                 ParameterKey=DBPassword,ParameterValue=%DB_PASSWORD% ^
    --capabilities CAPABILITY_NAMED_IAM
