# 后端接口测试方法总结

## 测试环境

- **Java版本**: JDK 21
- **Web服务器**: Apache Tomcat 11.0.7
- **应用路径**: `http://localhost:8080/groupproject/`

---

## 测试方法

### 1. 纯Java单元测试（不依赖Tomcat）

适用于数据访问层（DAO）的测试，使用纯Java代码直接调用。

```bash
# 编译
javac -encoding UTF-8 -d backend/build/classes \
  -cp "tomcat/lib/servlet-api.jar" \
  backend/src/com/example/authlogin/model/User.java \
  backend/src/com/example/authlogin/dao/UserDao.java

# 编译测试
javac -encoding UTF-8 -d backend/build/classes \
  -cp backend/build/classes \
  backend/test/com/example/authlogin/dao/UserDaoTest.java

# 运行测试（不需要Tomcat）
java -cp backend/build/classes com.example.authlogin.dao.UserDaoTest
```

### 2. HTTP接口测试（需要Tomcat运行）

适用于Servlet层测试，通过curl发送HTTP请求验证接口。

```bash
# 启动Tomcat后，测试注册接口
curl -s -X POST "http://localhost:8080/groupproject/register" \
  -d "username=testuser001&password=password123&confirmPassword=password123&email=test@example.com&role=TA"

# 测试登录接口
curl -s -c cookies.txt -X POST "http://localhost:8080/groupproject/login" \
  -d "username=testuser001&password=password123"

# 测试错误密码
curl -s -X POST "http://localhost:8080/groupproject/login" \
  -d "username=testuser001&password=wrongpassword"
```

---

## 部署步骤

### 方式一：手动部署（推荐测试用）

```bash
# 1. 编译Java文件
javac -encoding UTF-8 -d backend/build/classes \
  -cp "tomcat/lib/servlet-api.jar;backend/build/classes" \
  backend/src/com/example/authlogin/*.java

# 2. 部署到Tomcat
cp -r backend/build/classes/* tomcat/webapps/groupproject/WEB-INF/classes/

# 3. 触发热加载（可选）
touch tomcat/webapps/groupproject/WEB-INF/web.xml
```

### 方式二：使用脚本

```bash
# 运行构建脚本
scripts\build.bat

# 运行部署脚本
scripts\deploy.bat

# 启动Tomcat
scripts\startup.bat
```

---

## 测试结果示例

### 注册接口

| 输入 | 预期结果 | 实际响应 |
|------|----------|----------|
| 新用户 | 201 Created | `{"success": true, "userId": "..."}` |
| 已存在用户名 | 409 Conflict | `{"error": "Username already exists"}` |
| 密码不匹配 | 400 Bad Request | `{"error": "Passwords do not match"}` |

### 登录接口

| 输入 | 预期结果 | 实际响应 |
|------|----------|----------|
| 正确用户名密码 | 200 OK | `{"success": true, "username": "...", "role": "TA"}` |
| 错误密码 | 401 Unauthorized | `{"error": "Invalid username or password"}` |
| 空用户名 | 400 Bad Request | `{"error": "Username and password are required"}` |

---

## 注意事项

1. **数据文件位置**: `data/users.csv`（在项目根目录）
2. **清除测试数据**: 测试前删除 `data/users.csv`
3. **Tomcat热加载**: 修改Servlet后 `touch web.xml` 触发重新加载
4. **端口占用**: 如遇端口冲突，检查并关闭占用进程
