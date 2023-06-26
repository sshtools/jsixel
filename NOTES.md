# Notes

## Building Libsixel for various platforms

We need to build on various architectures and place the resultant shared libraries in `src/main/resources/[platform]`.

## Cross Compiling Libsixel for Windows 32 bit

```
CC=i686-w64-mingw32-gcc cross_compile=yes ./configure --host=i686-w64-mingw32 --without-png --without-libcurl --disable-python
make
cp src/.libs/libsixel-1.dll /path/to/jsixel/src/main/resources/win32-x86/sixel.dll
```

## Cross Compiling Libsixel for Windows 64 bit

**FAILS TO BUILD**

```
CC=x86_64-w64-mingw32-g++ cross_compile=yes ./configure --without-libcurl --disable-python --without-png --host=x86_64-w32-mingw64
make
```

results in.

```
                 from output.c:26:
/usr/lib/gcc/x86_64-w64-mingw32/12-win32/include/c++/cstdlib:151:11: error: ‘malloc’ has not been declared in ‘::’
  151 |   using ::malloc;
      |           ^~~~~~

```

## Using MSYS2 on Windows

As an alternative way to build 64 bit. Use MSYS2 on a Windows host. 

```
./configure --without-png --without-libcurl --disable-python --disable-dependency-tracking
make
cp src/.libs/libsixel-1.dll /path/to/jsixel/src/main/resources/win32-x86-64/sixel.dll
```


