package com.sea.odps.sql.core.util;

/**
 * 日志工具类，提供日志相关的工具方法。
 *
 * <p>用于简化日志记录，特别是获取当前方法名和对象类名。
 */
public final class LogUtil {

  /** 调用栈中调用者的索引位置。 调用栈：[0] getStackTrace, [1] methodName, [2] 调用者 所以需要访问索引 2。 */
  private static final int CALLER_INDEX = 2;

  /** 私有构造函数，防止实例化。 */
  private LogUtil() {
    throw new UnsupportedOperationException("工具类不允许实例化");
  }

  /**
   * 获取当前调用方法的方法名。
   *
   * @return 方法名，如果无法获取则返回 "unknown"
   */
  public static String methodName() {
    try {
      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
      if (stackTrace.length > CALLER_INDEX) {
        return stackTrace[CALLER_INDEX].getMethodName();
      }
    } catch (Exception e) {
      // 忽略异常，返回默认值
    }
    return "unknown";
  }

  /**
   * 获取对象的简单类名（不包含包名）。
   *
   * @param obj 目标对象
   * @return 简单类名，如果对象为 null 则返回 "null"
   */
  public static String simpleClassName(final Object obj) {
    if (obj == null) {
      return "null";
    }
    return obj.getClass().getSimpleName();
  }

  /**
   * 获取当前方法名和对象的类名组合，用于日志记录。 返回格式：methodName:className
   *
   * @param obj 目标对象
   * @return 方法名和类名的组合字符串
   */
  public static String methodAndClass(final Object obj) {
    return methodName() + ":" + simpleClassName(obj);
  }

  /**
   * 获取当前方法名和类名组合，用于日志记录。 返回格式：methodName:className
   *
   * @param className 类名字符串
   * @return 方法名和类名的组合字符串
   */
  public static String methodAndClass(final String className) {
    return methodName() + ":" + (className == null ? "null" : className);
  }
}
