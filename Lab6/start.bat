@echo off
echo ===========================================
echo Збірка мікросервісів (Maven)...
echo ===========================================

cd auth-service && call mvn clean package -DskipTests && cd ..
cd catalog-service && call mvn clean package -DskipTests && cd ..
cd order-service && call mvn clean package -DskipTests && cd ..
cd api-gateway && call mvn clean package -DskipTests && cd ..

echo ===========================================
echo Запуск Docker Compose...
echo ===========================================
docker-compose up --build -d

echo ===========================================
echo Готово! Сервіси піднімаються у фоні.
echo Щоб подивитись логи, використовуйте: docker-compose logs -f
echo ===========================================
pause