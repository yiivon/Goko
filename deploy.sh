#!/bin/bash
cd /home/travis/build/cncgoko/Goko/org.goko.build.product/target/repository/
find . -type f -exec echo {} {} \;
if [$updateRepository == 'true'] then
  echo "Exporting repository..."
  
  find . -type f -exec curl --ftp-create-dirs -T {} -u $VAR1:$VAR2 $TARGET/{} \;
fi
#curl --ftp-create-dirs -T $HOME/org.goko.build.product/target/products/org.goko-win32.win32.x86_64.zip -u $VAR1:$VAR2 $TARGET/org.goko-win32.win32.x86_64.zip
