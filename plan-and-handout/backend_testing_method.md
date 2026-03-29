# 后端接口测试方法（简单版）

## 测试环境

- **Java**: JDK 21
- **Tomcat**: 11.0.7
- **应用路径**: `http://localhost:8080/groupproject/`

---

## 部署步骤

```bash
# 编译并部署
javac -encoding UTF-8 -d backend/build/classes -cp "tomcat/lib/servlet-api.jar;backend/build/classes" backend/src/com/example/authlogin/*.java
cp -r backend/build/classes/* tomcat/webapps/groupproject/WEB-INF/classes/

# 启动Tomcat
scripts\startup.bat
```

---

## 测试方法

用curl简单测几个接口能跑通就行：

```bash
# 注册
curl -s -X POST "http://localhost:8080/groupproject/register" -d "username=testuser&password=123456&confirmPassword=123456&email@test.com&role=TA"

# 登录
curl -s -X POST "http://localhost:8080/groupproject/login" -d "username=testuser&password=123456"
```

---

## 注意

- 测试前删除 `data/users.csv` 清空数据
- 修改代码后重启Tomcat
