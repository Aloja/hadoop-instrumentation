# Set default path for all commands
Exec { path => [ "/bin/", "/sbin/" , "/usr/bin/", "/usr/sbin/" ] }


# Run this before anything else
stage { 'init':
    before => Stage['main'],
}
class { 'prepare': 
    stage => init,
}

class prepare {

    # Prefer ipv4 over ipv6 for faster downloads
    exec { 'prefer_ipv4':
        command => 'echo "\nprecedence ::ffff:0:0/96  100" >> /etc/gai.conf',
        unless => 'grep "^precedence ::ffff:0:0/96  100$" /etc/gai.conf',
    }


    # Configure apt to install less dependencies
    file { 'apt-settings':
        path => '/etc/apt/apt.conf.d/90localsettings',
        source  => '/vagrant/files/apt-90localsettings',
        owner => 'root',
        group => 'root',
    }

}


# Better apt mirrors
class { 'apt':
    purge => {
        'sources.list' => true,
        'sources.list.d' => true,
    },
}
apt::source { 'ubuntu_trusty':
    location          => 'http://ftp.udc.es/ubuntu/',
    release           => 'trusty',
    repos             => 'main restricted universe',
    include           => {
        'deb' => true,
        'src' => true,
    },
}
apt::source { 'ubuntu_trusty-updates':
    location          => 'http://ftp.udc.es/ubuntu/',
    release           => 'trusty-updates',
    repos             => 'main restricted universe',
    include           => {
        'deb' => true,
        'src' => true,
    },
}
apt::source { 'ubuntu_trusty-security':
    location          => 'http://security.ubuntu.com/ubuntu',
    release           => 'trusty-security',
    repos             => 'main restricted universe',
    include           => {
        'deb' => true,
        'src' => true,
    },
}


# Install various packages
package { ['ant', 'build-essential', 'binutils-dev', 'dh-autoreconf', 'git', 'less', 'libiberty-dev', 'libpcap-dev', 'libxml2-dev', 'openjdk-7-jdk', 'screen', 'sysstat', 'unzip', 'vim']:
    ensure => installed,
    require => Class['apt'],
}


# Remove unnecessary packages
package { ['landscape-client', 'landscape-common']:
    ensure => purged,
    require => Class['apt'],
}


# Remove motd message
file { '/home/vagrant/.hushlogin':
    ensure => present,
}


# Link mounted folder to home for easier access
file { '/home/vagrant/workspace':
    ensure => link,
    target => '/vagrant/workspace',
}


# Shell config
file { '/home/vagrant/.bash_aliases':
    source  => '/vagrant/files/bash_aliases',
}


# Configure passwordless SSH
file { '/home/vagrant/.ssh/id_rsa':
    source  => '/vagrant/files/id_rsa',
    mode => 600,
}
file { '/home/vagrant/.ssh/id_rsa.pub':
    source  => '/vagrant/files/id_rsa.pub',
}
exec { 'authorized_keys':
    user => 'vagrant',
    command => 'echo "`cat /vagrant/files/id_rsa.pub`" >> /home/vagrant/.ssh/authorized_keys',
    unless => 'grep "`cat /vagrant/files/id_rsa.pub`" /home/vagrant/.ssh/authorized_keys',
}