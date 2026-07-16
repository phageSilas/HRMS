import { shellRoutes } from './routeMeta';
import { history } from '@umijs/max';
import { Empty, Input, List, Modal, Space, Tag, Typography } from 'antd';
import React, { useMemo, useState } from 'react';
import styles from './index.less';

const { Text } = Typography;

interface GlobalSearchProps {
  open: boolean;
  onClose: () => void;
}

const GlobalSearch: React.FC<GlobalSearchProps> = ({ open, onClose }) => {
  const [keyword, setKeyword] = useState('');

  const results = useMemo(() => {
    const normalized = keyword.trim().toLowerCase();
    if (!normalized) {
      return shellRoutes.slice(0, 10);
    }
    return shellRoutes.filter((item) => {
      const haystack = [item.title, item.group, ...(item.keywords || [])]
        .join(' ')
        .toLowerCase();
      return haystack.includes(normalized);
    });
  }, [keyword]);

  const handleSelect = (path: string) => {
    history.push(path);
    setKeyword('');
    onClose();
  };

  return (
    <Modal
      open={open}
      title={null}
      footer={null}
      width={620}
      onCancel={onClose}
      destroyOnClose
      className={styles.searchModal}
    >
      <div className={styles.searchHeader}>
        <Input
          autoFocus
          allowClear
          size="large"
          placeholder="搜索模块、页面或业务功能"
          value={keyword}
          onChange={(event) => setKeyword(event.target.value)}
        />
        <Text type="secondary">Ctrl K</Text>
      </div>

      {results.length > 0 ? (
        <List
          className={styles.searchList}
          dataSource={results}
          renderItem={(item) => (
            <List.Item className={styles.searchItem} onClick={() => handleSelect(item.path)}>
              <Space size={12}>
                <span className={styles.searchIcon}>{item.icon}</span>
                <span>
                  <span className={styles.searchTitle}>{item.title}</span>
                  <Tag bordered={false} color="blue">
                    {item.group}
                  </Tag>
                </span>
              </Space>
            </List.Item>
          )}
        />
      ) : (
        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="没有找到匹配页面" />
      )}
    </Modal>
  );
};

export default GlobalSearch;
