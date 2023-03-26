# ChatApp

## 简介

Chatapp是一个基于Android Studio开发的即时通讯应用。它允许用户在移动设备上实时地与AI进行聊天。该应用具有高度可扩展的架构，通过反向代理实现网络连接。

## 特点

- 使用Android Studio进行开发，支持各种Android设备
- release版本可在`release`文件夹中找到
- 网络代理使用反向代理技术，提高了应用程序在特定地区的网络稳定性
- ChatGptService.java文件集成了网络代理与API调用功能，便于维护和扩展
- 使用OpenAi的GPT3.5模型进行聊天

## 如何安装

1. 从本项目的`release`文件夹中下载最新版本的Chatapp APK文件
2. 将APK文件传输到您的Android设备上
3. 在设备上找到并安装APK文件（可能需要开启“允许来自未知来源”的安装选项）
4. 安装完成后，打开Chatapp并按照提示进行注册/登录

## 如何使用

1. 打开Chatapp，在聊天框中输入你的聊天内容然后点击发送按钮。程序的状态会通过弹出的Token给出。
2. 删除聊天记录与截长图功能集成在左上角的Menu中。（目前截长图在鸿蒙系统中无法使用）



## Introduction

Chatapp is an instant messaging application developed in Android Studio that allows users to communicate in real-time with AI on their mobile devices. The app features a highly-scalable architecture and uses reverse proxy for network connectivity, enhancing stability in specific regions.

## Features

- Developed in Android Studio, supporting a wide range of Android devices
- Release version available in the `release` folder
- Network proxy utilizes reverse proxy technology, improving the app's network stability in specific regions
- ChatGptService.java file integrates network proxy and API call functionalities, facilitating maintenance and expansion
- Chat using OpenAI's GPT-3.5 model.

## Installation

1. Download the latest version of Chatapp APK file from the `release` folder in this project
2. Transfer the APK file to your Android device
3. Locate and install the APK file on your device (you may need to enable "Allow installation from unknown sources" option)
4. Once installed, open Chatapp and follow the prompts to register/login

## How to use

1. Open Chatapp, enter your chat content in the chat box, and tap the send button. The app's status will be provided through a pop-up token.
2. Delete chat history and long screenshot features are integrated in the menu located at the top-left corner (Note: long screenshot feature is currently not supported on HarmonyOS).

Please provide feedback and suggestions to help improve Chatapp. Thank you for using our app!
