#!/bin/bash

# 清理Git历史中的敏感信息
echo "开始清理Git历史中的敏感信息..."

# 使用git filter-branch清理包含敏感信息的提交
git filter-branch --force --index-filter \
'git rm --cached --ignore-unmatch http-mcp-client/src/main/resources/application.properties' \
--prune-empty --tag-name-filter cat -- --all

# 清理备份
rm -rf .git/refs/original/
git reflog expire --expire=now --all
git gc --prune=now --aggressive

echo "敏感信息清理完成！"
echo "请手动添加清理后的配置文件："
echo "1. 复制 application.properties.example 为 application.properties"
echo "2. 填入您的真实API密钥"
echo "3. 确保 application.properties 在 .gitignore 中" 